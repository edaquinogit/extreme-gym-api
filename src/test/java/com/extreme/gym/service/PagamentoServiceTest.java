package com.extreme.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.extreme.gym.dto.pagamento.PagamentoRequestDTO;
import com.extreme.gym.dto.pagamento.PagamentoResponseDTO;
import com.extreme.gym.entity.Aluno;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.entity.Plano;
import com.extreme.gym.enums.FormaPagamento;
import com.extreme.gym.enums.StatusAluno;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-27T12:00:00Z"), ZoneId.of("America/Bahia"));

    private PagamentoService pagamentoService;

    @BeforeEach
    void setUp() {
        pagamentoService = new PagamentoService(pagamentoRepository, matriculaRepository, clock);
    }

    @Test
    void deveRegistrarPagamentoComSucesso() {
        Long matriculaId = 1L;
        LocalDateTime dataCadastro = LocalDateTime.now(clock);
        PagamentoRequestDTO request = criarRequest(matriculaId);
        Matricula matricula = criarMatricula(matriculaId, StatusMatricula.ATIVA);

        when(matriculaRepository.findById(matriculaId)).thenReturn(Optional.of(matricula));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matriculaId, StatusPagamento.PAGO)).thenReturn(false);
        when(pagamentoRepository.saveAndFlush(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento pagamento = invocation.getArgument(0);
            pagamento.setId(1L);
            pagamento.setDataCadastro(dataCadastro);
            return pagamento;
        });

        PagamentoResponseDTO response = pagamentoService.registrar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.matriculaId()).isEqualTo(matriculaId);
        assertThat(response.alunoId()).isEqualTo(matricula.getAluno().getId());
        assertThat(response.alunoNome()).isEqualTo(matricula.getAluno().getNome());
        assertThat(response.planoId()).isEqualTo(matricula.getPlano().getId());
        assertThat(response.planoNome()).isEqualTo(matricula.getPlano().getNome());
        assertThat(response.valor()).isEqualByComparingTo(request.valor());
        assertThat(response.formaPagamento()).isEqualTo(FormaPagamento.PIX);
        assertThat(response.status()).isEqualTo(StatusPagamento.PAGO);
        assertThat(response.dataPagamento()).isNotNull();
        assertThat(response.dataCadastro()).isEqualTo(dataCadastro);
        verify(pagamentoRepository).saveAndFlush(any(Pagamento.class));
    }

    @Test
    void naoDeveRegistrarPagamentoParaMatriculaInexistente() {
        Long matriculaId = 99L;
        PagamentoRequestDTO request = criarRequest(matriculaId);

        when(matriculaRepository.findById(matriculaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.registrar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matricula nao encontrada com id: 99");

        verify(pagamentoRepository, never()).existsByMatriculaIdAndStatus(matriculaId, StatusPagamento.PAGO);
        verify(pagamentoRepository, never()).saveAndFlush(any(Pagamento.class));
    }

    @Test
    void naoDeveRegistrarPagamentoParaMatriculaCancelada() {
        Long matriculaId = 1L;
        PagamentoRequestDTO request = criarRequest(matriculaId);

        when(matriculaRepository.findById(matriculaId))
                .thenReturn(Optional.of(criarMatricula(matriculaId, StatusMatricula.CANCELADA)));

        assertThatThrownBy(() -> pagamentoService.registrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Matricula cancelada nao pode receber pagamento");

        verify(pagamentoRepository, never()).existsByMatriculaIdAndStatus(matriculaId, StatusPagamento.PAGO);
        verify(pagamentoRepository, never()).saveAndFlush(any(Pagamento.class));
    }

    @Test
    void naoDeveRegistrarPagamentoParaMatriculaVencida() {
        Long matriculaId = 1L;
        PagamentoRequestDTO request = criarRequest(matriculaId);

        when(matriculaRepository.findById(matriculaId))
                .thenReturn(Optional.of(criarMatricula(matriculaId, StatusMatricula.VENCIDA)));

        assertThatThrownBy(() -> pagamentoService.registrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Matricula vencida nao pode receber pagamento nesta versao");

        verify(pagamentoRepository, never()).existsByMatriculaIdAndStatus(matriculaId, StatusPagamento.PAGO);
        verify(pagamentoRepository, never()).saveAndFlush(any(Pagamento.class));
    }

    @Test
    void naoDeveRegistrarPagamentoDuplicadoPagoParaMesmaMatricula() {
        Long matriculaId = 1L;
        PagamentoRequestDTO request = criarRequest(matriculaId);

        when(matriculaRepository.findById(matriculaId))
                .thenReturn(Optional.of(criarMatricula(matriculaId, StatusMatricula.ATIVA)));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matriculaId, StatusPagamento.PAGO)).thenReturn(true);

        assertThatThrownBy(() -> pagamentoService.registrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Matricula ja possui pagamento pago registrado");

        verify(pagamentoRepository, never()).saveAndFlush(any(Pagamento.class));
    }

    @Test
    void deveTratarViolacaoDeConstraintAoRegistrarPagamentoPagoDuplicado() {
        Long matriculaId = 1L;
        PagamentoRequestDTO request = criarRequest(matriculaId);

        when(matriculaRepository.findById(matriculaId))
                .thenReturn(Optional.of(criarMatricula(matriculaId, StatusMatricula.ATIVA)));
        when(pagamentoRepository.existsByMatriculaIdAndStatus(matriculaId, StatusPagamento.PAGO)).thenReturn(false);
        when(pagamentoRepository.saveAndFlush(any(Pagamento.class)))
                .thenThrow(new DataIntegrityViolationException("uk_pagamentos_matricula_pago"));

        assertThatThrownBy(() -> pagamentoService.registrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Matricula ja possui pagamento pago registrado");
    }

    @Test
    void deveListarPagamentos() {
        Pagamento dinheiro = criarPagamento(1L, StatusPagamento.PAGO);
        Pagamento pix = criarPagamento(2L, StatusPagamento.CANCELADO);

        Pageable pageable = PageRequest.of(0, 20);
        when(pagamentoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(dinheiro, pix)));

        List<PagamentoResponseDTO> response = pagamentoService.listar(pageable);

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(PagamentoResponseDTO::id)
                .containsExactly(1L, 2L);
    }

    @Test
    void deveListarPagamentosPorMatricula() {
        Long matriculaId = 1L;
        Pagamento pagamento = criarPagamento(1L, StatusPagamento.PAGO);

        Pageable pageable = PageRequest.of(0, 20);
        when(pagamentoRepository.findByMatriculaId(matriculaId, pageable))
                .thenReturn(new PageImpl<>(List.of(pagamento)));

        List<PagamentoResponseDTO> response = pagamentoService.listarPorMatricula(matriculaId, pageable);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().matriculaId()).isEqualTo(matriculaId);
    }

    @Test
    void deveBuscarPagamentoPorId() {
        Long pagamentoId = 1L;
        Pagamento pagamento = criarPagamento(pagamentoId, StatusPagamento.PAGO);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        PagamentoResponseDTO response = pagamentoService.buscarPorId(pagamentoId);

        assertThat(response.id()).isEqualTo(pagamentoId);
        assertThat(response.matriculaId()).isEqualTo(pagamento.getMatricula().getId());
        assertThat(response.alunoNome()).isEqualTo(pagamento.getMatricula().getAluno().getNome());
        assertThat(response.planoNome()).isEqualTo(pagamento.getMatricula().getPlano().getNome());
        assertThat(response.status()).isEqualTo(StatusPagamento.PAGO);
    }

    @Test
    void deveLancarErroAoBuscarPagamentoInexistente() {
        Long pagamentoId = 99L;

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.buscarPorId(pagamentoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pagamento nao encontrado com id: 99");
    }

    @Test
    void deveCancelarPagamentoComSucesso() {
        Long pagamentoId = 1L;
        Pagamento pagamento = criarPagamento(pagamentoId, StatusPagamento.PAGO);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);

        PagamentoResponseDTO response = pagamentoService.cancelar(pagamentoId);

        assertThat(response.status()).isEqualTo(StatusPagamento.CANCELADO);
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.CANCELADO);
        verify(pagamentoRepository).save(pagamento);
        verify(pagamentoRepository, never()).delete(any(Pagamento.class));
    }

    @Test
    void naoDeveCancelarPagamentoInexistente() {
        Long pagamentoId = 99L;

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.cancelar(pagamentoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pagamento nao encontrado com id: 99");

        verify(pagamentoRepository, never()).save(any(Pagamento.class));
        verify(pagamentoRepository, never()).delete(any(Pagamento.class));
    }

    private PagamentoRequestDTO criarRequest(Long matriculaId) {
        return new PagamentoRequestDTO(
                matriculaId,
                BigDecimal.valueOf(99.90),
                FormaPagamento.PIX
        );
    }

    private Pagamento criarPagamento(Long id, StatusPagamento status) {
        return Pagamento.builder()
                .id(id)
                .matricula(criarMatricula(1L, StatusMatricula.ATIVA))
                .valor(BigDecimal.valueOf(99.90))
                .formaPagamento(FormaPagamento.PIX)
                .status(status)
                .dataPagamento(LocalDateTime.now(clock))
                .dataCadastro(LocalDateTime.now(clock))
                .build();
    }

    private Matricula criarMatricula(Long id, StatusMatricula status) {
        LocalDate dataInicio = LocalDate.of(2026, 5, 9);

        return Matricula.builder()
                .id(id)
                .aluno(criarAluno(1L))
                .plano(criarPlano(1L))
                .dataInicio(dataInicio)
                .dataFim(dataInicio.plusDays(30))
                .status(status)
                .dataCadastro(LocalDateTime.now(clock))
                .build();
    }

    private Aluno criarAluno(Long id) {
        Aluno aluno = Aluno.builder()
                .id(id)
                .nome("Ana Silva")
                .email("ana.silva@email.com")
                .telefone("71999990000")
                .status(StatusAluno.ATIVO)
                .build();
        aluno.setCriadoEm(LocalDateTime.now(clock));
        return aluno;
    }

    private Plano criarPlano(Long id) {
        return Plano.builder()
                .id(id)
                .nome("Plano Mensal")
                .descricao("Acesso livre por 30 dias")
                .valorMensal(BigDecimal.valueOf(99.90))
                .duracaoEmDias(30)
                .ativo(true)
                .dataCadastro(LocalDateTime.now(clock))
                .build();
    }
}
