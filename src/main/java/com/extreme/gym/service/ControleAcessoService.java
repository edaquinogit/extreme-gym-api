package com.extreme.gym.service;

import com.extreme.gym.dto.controleacesso.SnapshotAutorizadosResponseDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoRequestDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoResponseDTO;
import com.extreme.gym.dto.evento.EventoAcessoRequestDTO;
import com.extreme.gym.dto.evento.EventoAcessoResponseDTO;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.repository.CredencialAcessoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ControleAcessoService {

    private static final Logger log = LoggerFactory.getLogger(ControleAcessoService.class);

    private final DispositivoAcessoService dispositivoAcessoService;
    private final CredencialAcessoRepository credencialAcessoRepository;
    private final AcessoService acessoService;
    private final EventoAcessoService eventoAcessoService;
    private final AuditoriaAcessoService auditoriaAcessoService;
    private final Clock clock;

    @Value("${app.access-device.module-enabled:true}")
    private boolean moduloHabilitado;

    @Value("${app.access-device.api-key-required:true}")
    private boolean apiKeyObrigatoria;

    @Value("${app.access-snapshot.enabled:true}")
    private boolean snapshotHabilitado;

    @Value("${app.access-snapshot.validity-minutes:15}")
    private long validadeSnapshotMinutos;

    @Transactional
    public ValidarDispositivoResponseDTO validarDispositivo(ValidarDispositivoRequestDTO request, String apiKey) {
        validarModuloHabilitado();
        DispositivoAcesso dispositivo = dispositivoAcessoService.buscarEntidadePorId(request.dispositivoId());
        dispositivoAcessoService.validarApiKey(dispositivo, apiKey, apiKeyObrigatoria);
        dispositivoAcessoService.validarOperacional(dispositivo);

        CredencialAcesso credencial = credencialAcessoRepository.findByTipoAndIdentificadorExternoAndStatus(
                request.credencialTipo(),
                request.identificadorExterno(),
                StatusCredencialAcesso.ATIVA
        ).orElseThrow(() -> new BusinessException("Credencial de acesso ativa nao encontrada"));

        AcessoService.ResultadoAcesso resultado = acessoService.validarAluno(credencial.getAlunoId());
        ResultadoAcesso resultadoEvento = Boolean.TRUE.equals(resultado.acessoLiberado())
                ? ResultadoAcesso.LIBERADO
                : ResultadoAcesso.BLOQUEADO;
        EventoAcessoResponseDTO evento = eventoAcessoService.registrar(new EventoAcessoRequestDTO(
                resultado.aluno().getId(),
                dispositivo.getId(),
                resultado.matricula() != null ? resultado.matricula().getId() : null,
                request.origem(),
                ModoEventoAcesso.ONLINE,
                resultadoEvento,
                resultado.motivo(),
                request.dataHoraEvento(),
                true,
                null,
                request.idempotencyKey()
        ));

        auditoriaAcessoService.registrar("VALIDAR_ACESSO_DISPOSITIVO", "EventoAcesso", evento.id(), dispositivo.getId(),
                request.origem().name(), "Validacao por dispositivo resultou em " + resultadoEvento);
        log.info("Validacao por dispositivo concluida: dispositivoId={}, alunoId={}, resultado={}",
                dispositivo.getId(), resultado.aluno().getId(), resultadoEvento);

        Matricula matricula = resultado.matricula();
        return new ValidarDispositivoResponseDTO(
                resultado.acessoLiberado(),
                resultadoEvento,
                resultado.motivo(),
                new ValidarDispositivoResponseDTO.AlunoResumo(resultado.aluno().getId(), resultado.aluno().getNome()),
                matricula == null ? null : new ValidarDispositivoResponseDTO.MatriculaResumo(
                        matricula.getId(), matricula.getDataFim()),
                matricula == null ? null : new ValidarDispositivoResponseDTO.PlanoResumo(
                        matricula.getPlano().getId(), matricula.getPlano().getNome()),
                new ValidarDispositivoResponseDTO.DispositivoResumo(
                        dispositivo.getId(), dispositivo.getNome(), dispositivo.getStatus().name()),
                evento.id(),
                LocalDateTime.now(clock)
        );
    }

    public SnapshotAutorizadosResponseDTO gerarSnapshot(Long dispositivoId) {
        validarModuloHabilitado();
        if (!snapshotHabilitado) {
            throw new BusinessException("Snapshot de acesso esta desabilitado");
        }
        if (dispositivoId != null) {
            DispositivoAcesso dispositivo = dispositivoAcessoService.buscarEntidadePorId(dispositivoId);
            dispositivoAcessoService.validarOperacional(dispositivo);
        }

        LocalDateTime geradoEm = LocalDateTime.now(clock);
        LocalDateTime validoAte = geradoEm.plusMinutes(validadeSnapshotMinutos);
        List<SnapshotAutorizadosResponseDTO.Item> itens = credencialAcessoRepository
                .findByStatus(StatusCredencialAcesso.ATIVA)
                .stream()
                .map(credencial -> toSnapshotItem(credencial, validoAte))
                .toList();
        long liberados = itens.stream().filter(item -> Boolean.TRUE.equals(item.liberado())).count();

        log.info("Snapshot de autorizados gerado: totalCredenciais={}, totalLiberados={}", itens.size(), liberados);
        return new SnapshotAutorizadosResponseDTO(
                "access-snapshot-" + geradoEm.toLocalDate(),
                geradoEm,
                validoAte,
                itens.size(),
                liberados,
                itens.size() - liberados,
                itens
        );
    }

    private SnapshotAutorizadosResponseDTO.Item toSnapshotItem(CredencialAcesso credencial, LocalDateTime validoAte) {
        AcessoService.ResultadoAcesso resultado = acessoService.validarAluno(credencial.getAlunoId());
        boolean liberado = Boolean.TRUE.equals(resultado.acessoLiberado());
        return new SnapshotAutorizadosResponseDTO.Item(
                credencial.getAlunoId(),
                credencial.getTipo(),
                credencial.getIdentificadorExterno(),
                liberado,
                liberado ? null : resultado.motivo(),
                validoAte,
                credencial.getAtualizadoEm()
        );
    }

    private void validarModuloHabilitado() {
        if (!moduloHabilitado) {
            throw new BusinessException("Modulo de dispositivo de acesso esta desabilitado");
        }
    }
}
