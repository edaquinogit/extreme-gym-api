package com.extreme.gym.service;

import com.extreme.gym.dto.controleacesso.CapturaFaceRequestDTO;
import com.extreme.gym.dto.controleacesso.SnapshotAutorizadoItemDTO;
import com.extreme.gym.dto.controleacesso.SnapshotAutorizadosResponseDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoRequestDTO;
import com.extreme.gym.dto.controleacesso.ValidarDispositivoResponseDTO;
import com.extreme.gym.dto.credencial.CredencialAcessoResponseDTO;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.entity.DispositivoAcesso;
import com.extreme.gym.entity.EventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CredencialAcessoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ControleAcessoService {

    private final CredencialAcessoRepository credencialRepository;
    private final AlunoRepository alunoRepository;
    private final AcessoService acessoService;
    private final DeviceApiKeyAuthenticator deviceApiKeyAuthenticator;
    private final EventoAcessoService eventoAcessoService;

    @Value("${app.access.snapshot-ttl-minutes:360}")
    private long snapshotTtlMinutes;

    @Transactional(readOnly = true)
    public SnapshotAutorizadosResponseDTO gerarSnapshot(DispositivoAcesso dispositivo) {
        deviceApiKeyAuthenticator.ensureAutomaticAccessAllowed(dispositivo);

        LocalDateTime geradoEm = LocalDateTime.now();
        LocalDateTime validoAte = geradoEm.plusMinutes(snapshotTtlMinutes);
        List<SnapshotAutorizadoItemDTO> itens = new ArrayList<>();
        int liberados = 0;
        int bloqueados = 0;

        for (CredencialAcesso credencial : credencialRepository.findByStatus(StatusCredencialAcesso.ATIVA)) {
            SnapshotAutorizadoItemDTO item = toSnapshotItem(credencial, validoAte);
            if (item.liberado()) {
                liberados++;
            } else {
                bloqueados++;
            }
            itens.add(item);
        }

        return new SnapshotAutorizadosResponseDTO(
                String.valueOf(geradoEm.withNano(0)),
                geradoEm,
                validoAte,
                itens.size(),
                liberados,
                bloqueados,
                itens
        );
    }

    public ValidarDispositivoResponseDTO validarDispositivo(
            DispositivoAcesso dispositivo,
            ValidarDispositivoRequestDTO request
    ) {
        deviceApiKeyAuthenticator.ensureAutomaticAccessAllowed(dispositivo);

        CredencialAcesso credencial = credencialRepository
                .findByTipoAndIdentificadorExterno(request.credencialTipo(), request.identificadorExterno())
                .orElse(null);

        if (credencial == null) {
            EventoAcesso evento = eventoAcessoService.registrarValidacao(
                    dispositivo,
                    request.idempotencyKey(),
                    null,
                    null,
                    request.origem(),
                    request.credencialTipo(),
                    request.identificadorExterno(),
                    ResultadoAcesso.BLOQUEADO,
                    "Credencial nao encontrada",
                    request.dataHoraEvento()
            );
            return new ValidarDispositivoResponseDTO(
                    false,
                    ResultadoAcesso.BLOQUEADO,
                    "Credencial nao encontrada",
                    null,
                    null,
                    evento.getId(),
                    evento.getDataHoraEvento()
            );
        }

        if (credencial.getStatus() != StatusCredencialAcesso.ATIVA) {
            EventoAcesso evento = eventoAcessoService.registrarValidacao(
                    dispositivo,
                    request.idempotencyKey(),
                    credencial.getAlunoId(),
                    null,
                    request.origem(),
                    credencial.getTipo(),
                    credencial.getIdentificadorExterno(),
                    ResultadoAcesso.BLOQUEADO,
                    "Credencial sem status ativo",
                    request.dataHoraEvento()
            );
            return new ValidarDispositivoResponseDTO(
                    false,
                    ResultadoAcesso.BLOQUEADO,
                    "Credencial sem status ativo",
                    credencial.getAlunoId(),
                    null,
                    evento.getId(),
                    evento.getDataHoraEvento()
            );
        }

        AcessoService.ResultadoAcesso validacao = acessoService.validarAluno(credencial.getAlunoId());
        ResultadoAcesso resultado = validacao.acessoLiberado() ? ResultadoAcesso.LIBERADO : ResultadoAcesso.BLOQUEADO;
        EventoAcesso evento = eventoAcessoService.registrarValidacao(
                dispositivo,
                request.idempotencyKey(),
                credencial.getAlunoId(),
                validacao.matricula() != null ? validacao.matricula().getId() : null,
                request.origem(),
                credencial.getTipo(),
                credencial.getIdentificadorExterno(),
                resultado,
                validacao.motivo(),
                request.dataHoraEvento()
        );

        return new ValidarDispositivoResponseDTO(
                validacao.acessoLiberado(),
                resultado,
                validacao.motivo(),
                validacao.aluno().getId(),
                validacao.aluno().getNome(),
                evento.getId(),
                evento.getDataHoraEvento()
        );
    }

    public CredencialAcessoResponseDTO capturarFace(DispositivoAcesso dispositivo, CapturaFaceRequestDTO request) {
        deviceApiKeyAuthenticator.ensureAutomaticAccessAllowed(dispositivo);

        if (!alunoRepository.existsById(request.alunoId())) {
            throw new ResourceNotFoundException("Aluno nao encontrado com id: " + request.alunoId());
        }

        String template = request.faceTemplate();
        if (template.toLowerCase().startsWith("data:image") || template.length() > 512) {
            throw new BusinessException("Face template deve ser uma referencia externa valida");
        }

        if (credencialRepository.existsByTipoAndIdentificadorExterno(TipoCredencialAcesso.FACE_TEMPLATE, template)) {
            throw new BusinessException("Template facial ja registrado no sistema");
        }

        CredencialAcesso credencial = CredencialAcesso.builder()
                .alunoId(request.alunoId())
                .tipo(TipoCredencialAcesso.FACE_TEMPLATE)
                .identificadorExterno(template)
                .fornecedor("DISPOSITIVO_" + dispositivo.getId())
                .status(StatusCredencialAcesso.ATIVA)
                .build();

        CredencialAcesso salva = credencialRepository.save(credencial);

        return new CredencialAcessoResponseDTO(
                salva.getId(),
                salva.getAlunoId(),
                salva.getTipo(),
                salva.getIdentificadorExterno(),
                salva.getFornecedor(),
                salva.getStatus(),
                salva.getCadastradoEm(),
                salva.getRevogadoEm(),
                salva.getTermoAceitoEm(),
                salva.getVersaoTermo(),
                salva.getCriadoEm(),
                salva.getAtualizadoEm()
        );
    }

    private SnapshotAutorizadoItemDTO toSnapshotItem(CredencialAcesso credencial, LocalDateTime validoAte) {
        try {
            AcessoService.ResultadoAcesso validacao = acessoService.validarAluno(credencial.getAlunoId());
            return new SnapshotAutorizadoItemDTO(
                    credencial.getAlunoId(),
                    credencial.getTipo(),
                    credencial.getIdentificadorExterno(),
                    validacao.acessoLiberado(),
                    validacao.acessoLiberado() ? null : validacao.motivo(),
                    validoAte,
                    credencial.getAtualizadoEm()
            );
        } catch (ResourceNotFoundException exception) {
            return new SnapshotAutorizadoItemDTO(
                    credencial.getAlunoId(),
                    credencial.getTipo(),
                    credencial.getIdentificadorExterno(),
                    false,
                    "Aluno nao encontrado",
                    validoAte,
                    credencial.getAtualizadoEm()
            );
        }
    }
}
