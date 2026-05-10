package com.extreme.gym.service;

import com.extreme.gym.dto.pagamento.PagamentoRequestDTO;
import com.extreme.gym.dto.pagamento.PagamentoResponseDTO;
import com.extreme.gym.entity.Matricula;
import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.enums.StatusMatricula;
import com.extreme.gym.enums.StatusPagamento;
import com.extreme.gym.exception.BusinessException;
import com.extreme.gym.exception.ResourceNotFoundException;
import com.extreme.gym.repository.MatriculaRepository;
import com.extreme.gym.repository.PagamentoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final MatriculaRepository matriculaRepository;

    public PagamentoResponseDTO registrar(PagamentoRequestDTO request) {
        Matricula matricula = buscarMatriculaPorId(request.matriculaId());

        validarMatriculaAtiva(matricula);
        validarPagamentoPagoDuplicado(matricula.getId());

        Pagamento pagamento = Pagamento.builder()
                .matricula(matricula)
                .valor(request.valor())
                .formaPagamento(request.formaPagamento())
                .status(StatusPagamento.PAGO)
                .dataPagamento(LocalDateTime.now())
                .build();

        return toResponseDTO(pagamentoRepository.save(pagamento));
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listar() {
        return pagamentoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPorMatricula(Long matriculaId) {
        return pagamentoRepository.findByMatriculaId(matriculaId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    public PagamentoResponseDTO cancelar(Long id) {
        Pagamento pagamento = buscarEntidadePorId(id);
        pagamento.setStatus(StatusPagamento.CANCELADO);

        return toResponseDTO(pagamentoRepository.save(pagamento));
    }

    private Matricula buscarMatriculaPorId(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matricula nao encontrada com id: " + id));
    }

    private Pagamento buscarEntidadePorId(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado com id: " + id));
    }

    private void validarMatriculaAtiva(Matricula matricula) {
        if (matricula.getStatus() == StatusMatricula.CANCELADA) {
            throw new BusinessException("Matricula cancelada nao pode receber pagamento");
        }
        if (matricula.getStatus() == StatusMatricula.VENCIDA) {
            throw new BusinessException("Matricula vencida nao pode receber pagamento nesta versao");
        }
    }

    private void validarPagamentoPagoDuplicado(Long matriculaId) {
        if (pagamentoRepository.existsByMatriculaIdAndStatus(matriculaId, StatusPagamento.PAGO)) {
            throw new BusinessException("Matricula ja possui pagamento pago registrado");
        }
    }

    private PagamentoResponseDTO toResponseDTO(Pagamento pagamento) {
        Matricula matricula = pagamento.getMatricula();

        return new PagamentoResponseDTO(
                pagamento.getId(),
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAluno().getNome(),
                matricula.getPlano().getId(),
                matricula.getPlano().getNome(),
                pagamento.getValor(),
                pagamento.getFormaPagamento(),
                pagamento.getStatus(),
                pagamento.getDataPagamento(),
                pagamento.getDataCadastro()
        );
    }
}
