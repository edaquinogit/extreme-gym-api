package com.extreme.gym.service;

import com.extreme.gym.dto.acesso.AcessoRequestDTO;
import com.extreme.gym.dto.acesso.AcessoResponseDTO;
import com.extreme.gym.dto.acesso.AssistenciaPinRequestDTO;
import com.extreme.gym.dto.acesso.AssistenciaPinResponseDTO;
import com.extreme.gym.dto.acesso.FichaAcessoResponseDTO;
import com.extreme.gym.dto.acesso.LiberacaoManualRequestDTO;
import com.extreme.gym.entity.CheckIn;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.entity.EventoAcesso;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.enums.ModoEventoAcesso;
import com.extreme.gym.enums.OrigemEventoAcesso;
import com.extreme.gym.enums.ResultadoAcesso;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.enums.TipoCredencialAcesso;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CheckInRepository;
import com.extreme.gym.repository.CredencialAcessoRepository;
import com.extreme.gym.repository.EventoAcessoRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcessoService {

    private static final String MOTIVO_LIBERADO = "Acesso liberado";

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CredencialAcessoRepository credencialRepository;
    private final EventoAcessoRepository eventoAcessoRepository;
    private final CheckInRepository checkInRepository;
    private final Clock clock;

    public AcessoResponseDTO validar(AcessoRequestDTO request) {
        return toResponseDTO(validarAluno(request.alunoId()));
    }

    public ResultadoAcesso validarAluno(Long alunoId) {
        Aluno aluno = buscarAlunoPorId(alunoId);

        if (aluno.getStatus() == StatusAluno.BLOQUEADO) {
            return ResultadoAcesso.bloqueado(aluno, "Aluno bloqueado");
        }
        if (aluno.getStatus() == StatusAluno.CANCELADO) {
            return ResultadoAcesso.bloqueado(aluno, "Aluno cancelado");
        }
        if (aluno.getStatus() == StatusAluno.INADIMPLENTE) {
            return ResultadoAcesso.bloqueado(aluno, "Aluno inadimplente");
        }

        return matriculaRepository.findByAlunoIdAndStatus(aluno.getId(), StatusMatricula.ATIVA)
                .map(matricula -> validarMatricula(aluno, matricula))
                .orElseGet(() -> ResultadoAcesso.bloqueado(aluno, "Aluno nao possui matricula ativa"));
    }

    public AssistenciaPinResponseDTO validarPinAssistido(AssistenciaPinRequestDTO request) {
        Aluno aluno = buscarAlunoPorId(request.alunoId());
        boolean valido = credencialRepository
                .findByAlunoIdAndTipoAndStatus(aluno.getId(), TipoCredencialAcesso.PIN, StatusCredencialAcesso.ATIVA)
                .map(credencial -> credencial.getIdentificadorExterno().equals(request.pin()))
                .orElse(false);

        return new AssistenciaPinResponseDTO(aluno.getId(), aluno.getNome(), valido);
    }

    public FichaAcessoResponseDTO buscarFichaAcesso(Long alunoId) {
        Aluno aluno = buscarAlunoPorId(alunoId);
        Matricula matriculaAtiva = matriculaRepository
                .findByAlunoIdAndStatus(aluno.getId(), StatusMatricula.ATIVA)
                .orElse(null);
        Pagamento ultimoPagamento = matriculaAtiva == null
                ? null
                : pagamentoRepository.findByMatriculaId(matriculaAtiva.getId()).stream()
                        .max(Comparator.comparing(this::dataPagamentoOrdenacao))
                        .orElse(null);
        List<CredencialAcesso> credenciaisAtivas = credencialRepository
                .findByAlunoIdAndStatus(aluno.getId(), StatusCredencialAcesso.ATIVA);
        ResultadoAcesso resultado = validarAluno(aluno.getId());

        return new FichaAcessoResponseDTO(
                toAlunoResumo(aluno),
                matriculaAtiva != null ? toMatriculaResumo(matriculaAtiva) : null,
                ultimoPagamento != null ? toPagamentoResumo(ultimoPagamento) : null,
                credenciaisAtivas.stream().map(this::toCredencialResumo).toList(),
                resultado.acessoLiberado() ? List.of() : List.of(resultado.motivo()),
                acoesPermitidas(resultado)
        );
    }

    @Transactional
    public AcessoResponseDTO liberarManual(LiberacaoManualRequestDTO request) {
        Aluno aluno = buscarAlunoPorId(request.alunoId());
        Matricula matricula = matriculaRepository
                .findByAlunoIdAndStatus(aluno.getId(), StatusMatricula.ATIVA)
                .orElse(null);
        String motivo = normalizarMotivoLiberacao(request);
        LocalDateTime agora = LocalDateTime.now(clock);

        eventoAcessoRepository.save(EventoAcesso.builder()
                .alunoId(aluno.getId())
                .dispositivoId(0L)
                .matriculaId(matricula != null ? matricula.getId() : null)
                .origem(OrigemEventoAcesso.MANUAL)
                .modo(ModoEventoAcesso.ONLINE)
                .resultado(com.extreme.gym.enums.ResultadoAcesso.LIBERADO)
                .motivo(motivo)
                .dataHoraEvento(agora)
                .idempotencyKey("manual-" + UUID.randomUUID())
                .sincronizado(true)
                .build());

        if (Boolean.TRUE.equals(request.registrarCheckin())) {
            checkInRepository.save(CheckIn.builder()
                    .aluno(aluno)
                    .matricula(matricula)
                    .dataHora(agora)
                    .permitido(true)
                    .motivo(motivo)
                    .build());
        }

        return new AcessoResponseDTO(
                aluno.getId(),
                aluno.getNome(),
                true,
                motivo,
                matricula != null ? matricula.getId() : null,
                matricula != null ? matricula.getDataFim() : null
        );
    }

    private Aluno buscarAlunoPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno nao encontrado com id: " + id));
    }

    private ResultadoAcesso validarMatricula(Aluno aluno, Matricula matricula) {
        if (matricula.getDataFim().isBefore(LocalDate.now(clock))) {
            return ResultadoAcesso.bloqueado(aluno, matricula, "Matricula vencida");
        }
        if (!pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO)) {
            return ResultadoAcesso.bloqueado(aluno, matricula, "Matricula nao possui pagamento pago");
        }

        return ResultadoAcesso.liberado(aluno, matricula);
    }

    private AcessoResponseDTO toResponseDTO(ResultadoAcesso resultado) {
        Matricula matricula = resultado.matricula();

        return new AcessoResponseDTO(
                resultado.aluno().getId(),
                resultado.aluno().getNome(),
                resultado.acessoLiberado(),
                resultado.motivo(),
                matricula != null ? matricula.getId() : null,
                matricula != null ? matricula.getDataFim() : null
        );
    }

    private FichaAcessoResponseDTO.AlunoResumoDTO toAlunoResumo(Aluno aluno) {
        return new FichaAcessoResponseDTO.AlunoResumoDTO(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getTelefone(),
                aluno.getStatus(),
                aluno.getCriadoEm()
        );
    }

    private FichaAcessoResponseDTO.MatriculaResumoDTO toMatriculaResumo(Matricula matricula) {
        return new FichaAcessoResponseDTO.MatriculaResumoDTO(
                matricula.getId(),
                matricula.getPlano().getId(),
                matricula.getPlano().getNome(),
                matricula.getDataInicio(),
                matricula.getDataFim(),
                matricula.getStatus()
        );
    }

    private FichaAcessoResponseDTO.PagamentoResumoDTO toPagamentoResumo(Pagamento pagamento) {
        return new FichaAcessoResponseDTO.PagamentoResumoDTO(
                pagamento.getId(),
                pagamento.getMatricula().getId(),
                pagamento.getValor(),
                pagamento.getFormaPagamento(),
                pagamento.getStatus(),
                pagamento.getDataPagamento(),
                pagamento.getDataCadastro()
        );
    }

    private FichaAcessoResponseDTO.CredencialResumoDTO toCredencialResumo(CredencialAcesso credencial) {
        return new FichaAcessoResponseDTO.CredencialResumoDTO(
                credencial.getId(),
                credencial.getTipo(),
                credencial.getFornecedor(),
                credencial.getStatus(),
                credencial.getCadastradoEm()
        );
    }

    private LocalDateTime dataPagamentoOrdenacao(Pagamento pagamento) {
        if (pagamento.getDataPagamento() != null) {
            return pagamento.getDataPagamento();
        }
        return pagamento.getDataCadastro() != null ? pagamento.getDataCadastro() : LocalDateTime.MIN;
    }

    private List<String> acoesPermitidas(ResultadoAcesso resultado) {
        if (resultado.acessoLiberado()) {
            return List.of("BLOQUEAR");
        }
        return List.of("BLOQUEAR", "MARCAR_PAGAMENTO_PENDENTE", "APROVAR_CADASTRO", "LIBERAR_MANUALMENTE");
    }

    private String normalizarMotivoLiberacao(LiberacaoManualRequestDTO request) {
        String motivo = request.motivo().trim();
        if (request.observacao() == null || request.observacao().isBlank()) {
            return motivo;
        }
        return motivo + " - " + request.observacao().trim();
    }

    public record ResultadoAcesso(
            Aluno aluno,
            Matricula matricula,
            Boolean acessoLiberado,
            String motivo
    ) {

        private static ResultadoAcesso liberado(Aluno aluno, Matricula matricula) {
            return new ResultadoAcesso(aluno, matricula, true, MOTIVO_LIBERADO);
        }

        private static ResultadoAcesso bloqueado(Aluno aluno, String motivo) {
            return bloqueado(aluno, null, motivo);
        }

        private static ResultadoAcesso bloqueado(Aluno aluno, Matricula matricula, String motivo) {
            return new ResultadoAcesso(aluno, matricula, false, motivo);
        }
    }
}
