package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.checkin.CheckInRequestDTO;
import com.extreme.gym.dto.checkin.CheckInResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.CheckIn;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.AlunoRepository;
import com.extreme.gym.repository.CheckInRepository;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private CheckInService checkInService;

    @Test
    void deveRegistrarCheckInPermitidoComSucesso() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(1L, aluno, LocalDate.now().plusDays(30));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO))
                .thenReturn(true);
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> {
            CheckIn checkIn = invocation.getArgument(0);
            checkIn.setId(1L);
            return checkIn;
        });

        CheckInResponseDTO response = checkInService.registrar(new CheckInRequestDTO(alunoId));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.alunoId()).isEqualTo(alunoId);
        assertThat(response.alunoNome()).isEqualTo(aluno.getNome());
        assertThat(response.matriculaId()).isEqualTo(matricula.getId());
        assertThat(response.permitido()).isTrue();
        assertThat(response.motivo()).isEqualTo("Check-in permitido");
        assertThat(response.dataHora()).isNotNull();
        verify(checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void naoDeveRegistrarCheckInParaAlunoInexistente() {
        Long alunoId = 99L;

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInService.registrar(new CheckInRequestDTO(alunoId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno nao encontrado com id: 99");

        verify(checkInRepository, never()).save(any(CheckIn.class));
    }

    @Test
    void deveBloquearCheckInParaAlunoBloqueado() {
        CheckInResponseDTO response = registrarCheckInBloqueadoPorStatus(StatusAluno.BLOQUEADO);

        assertThat(response.permitido()).isFalse();
        assertThat(response.matriculaId()).isNull();
        assertThat(response.motivo()).isEqualTo("Aluno bloqueado");
        verify(matriculaRepository, never()).findByAlunoIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearCheckInParaAlunoCancelado() {
        CheckInResponseDTO response = registrarCheckInBloqueadoPorStatus(StatusAluno.CANCELADO);

        assertThat(response.permitido()).isFalse();
        assertThat(response.matriculaId()).isNull();
        assertThat(response.motivo()).isEqualTo("Aluno cancelado");
        verify(matriculaRepository, never()).findByAlunoIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearCheckInParaAlunoInadimplente() {
        CheckInResponseDTO response = registrarCheckInBloqueadoPorStatus(StatusAluno.INADIMPLENTE);

        assertThat(response.permitido()).isFalse();
        assertThat(response.matriculaId()).isNull();
        assertThat(response.motivo()).isEqualTo("Aluno inadimplente");
        verify(matriculaRepository, never()).findByAlunoIdAndStatus(any(), any());
    }

    @Test
    void deveBloquearCheckInQuandoAlunoNaoPossuiMatriculaAtiva() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.empty());
        mockSave();

        CheckInResponseDTO response = checkInService.registrar(new CheckInRequestDTO(alunoId));

        assertThat(response.permitido()).isFalse();
        assertThat(response.matriculaId()).isNull();
        assertThat(response.motivo()).isEqualTo("Aluno nao possui matricula ativa");
        verify(checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void deveBloquearCheckInQuandoMatriculaEstaVencida() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(1L, aluno, LocalDate.now().minusDays(1));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        mockSave();

        CheckInResponseDTO response = checkInService.registrar(new CheckInRequestDTO(alunoId));

        assertThat(response.permitido()).isFalse();
        assertThat(response.matriculaId()).isNull();
        assertThat(response.motivo()).isEqualTo("Matricula vencida");
        verify(pagamentoRepository, never()).existsByMatriculaIdAndStatus(any(), any());
        verify(checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void deveBloquearCheckInQuandoMatriculaNaoPossuiPagamentoPago() {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, StatusAluno.ATIVO);
        Matricula matricula = criarMatricula(1L, aluno, LocalDate.now().plusDays(30));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.findByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVA))
                .thenReturn(Optional.of(matricula));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matricula.getId(), StatusPagamento.PAGO))
                .thenReturn(false);
        mockSave();

        CheckInResponseDTO response = checkInService.registrar(new CheckInRequestDTO(alunoId));

        assertThat(response.permitido()).isFalse();
        assertThat(response.matriculaId()).isNull();
        assertThat(response.motivo()).isEqualTo("Matricula nao possui pagamento pago");
        verify(checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void deveListarCheckIns() {
        CheckIn permitido = criarCheckIn(1L, true, "Check-in permitido");
        CheckIn bloqueado = criarCheckIn(2L, false, "Aluno inadimplente");

        when(checkInRepository.findAll()).thenReturn(List.of(permitido, bloqueado));

        List<CheckInResponseDTO> response = checkInService.listar();

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(CheckInResponseDTO::id)
                .containsExactly(1L, 2L);
    }

    @Test
    void deveListarCheckInsPorAluno() {
        Long alunoId = 1L;
        CheckIn checkIn = criarCheckIn(1L, true, "Check-in permitido");

        when(checkInRepository.findByAlunoId(alunoId)).thenReturn(List.of(checkIn));

        List<CheckInResponseDTO> response = checkInService.listarPorAluno(alunoId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().alunoId()).isEqualTo(alunoId);
    }

    @Test
    void deveBuscarCheckInPorId() {
        Long checkInId = 1L;
        CheckIn checkIn = criarCheckIn(checkInId, true, "Check-in permitido");

        when(checkInRepository.findById(checkInId)).thenReturn(Optional.of(checkIn));

        CheckInResponseDTO response = checkInService.buscarPorId(checkInId);

        assertThat(response.id()).isEqualTo(checkInId);
        assertThat(response.alunoId()).isEqualTo(checkIn.getAluno().getId());
        assertThat(response.alunoNome()).isEqualTo(checkIn.getAluno().getNome());
        assertThat(response.matriculaId()).isEqualTo(checkIn.getMatricula().getId());
        assertThat(response.permitido()).isTrue();
    }

    @Test
    void deveLancarErroAoBuscarCheckInInexistente() {
        Long checkInId = 99L;

        when(checkInRepository.findById(checkInId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInService.buscarPorId(checkInId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Check-in nao encontrado com id: 99");
    }

    private CheckInResponseDTO registrarCheckInBloqueadoPorStatus(StatusAluno status) {
        Long alunoId = 1L;
        Aluno aluno = criarAluno(alunoId, status);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        mockSave();

        return checkInService.registrar(new CheckInRequestDTO(alunoId));
    }

    private void mockSave() {
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> {
            CheckIn checkIn = invocation.getArgument(0);
            checkIn.setId(1L);
            return checkIn;
        });
    }

    private CheckIn criarCheckIn(Long id, Boolean permitido, String motivo) {
        Aluno aluno = criarAluno(1L, StatusAluno.ATIVO);

        return CheckIn.builder()
                .id(id)
                .aluno(aluno)
                .matricula(permitido ? criarMatricula(1L, aluno, LocalDate.now().plusDays(30)) : null)
                .permitido(permitido)
                .motivo(motivo)
                .dataHora(LocalDateTime.now())
                .build();
    }

    private Aluno criarAluno(Long id, StatusAluno status) {
        return Aluno.builder()
                .id(id)
                .nome("Ana Silva")
                .email("ana.silva@email.com")
                .telefone("71999990000")
                .status(status)
                .dataCadastro(LocalDateTime.now())
                .build();
    }

    private Matricula criarMatricula(Long id, Aluno aluno, LocalDate dataFim) {
        return Matricula.builder()
                .id(id)
                .aluno(aluno)
                .plano(criarPlano(1L))
                .dataInicio(LocalDate.now())
                .dataFim(dataFim)
                .status(StatusMatricula.ATIVA)
                .dataCadastro(LocalDateTime.now())
                .build();
    }

    private Plano criarPlano(Long id) {
        return Plano.builder()
                .id(id)
                .nome("Plano Mensal")
                .descricao("Acesso livre por 30 dias")
                .valorMensal(BigDecimal.valueOf(99.90))
                .duracaoEmDias(30)
                .ativo(true)
                .dataCadastro(LocalDateTime.now())
                .build();
    }
}
