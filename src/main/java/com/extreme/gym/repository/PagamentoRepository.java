package com.extreme.gym.repository;

import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.enums.StatusPagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Page<Pagamento> findByMatriculaId(Long matriculaId, Pageable pageable);

    boolean existsByMatriculaIdAndStatus(Long matriculaId, StatusPagamento status);
}
