package com.extreme.gym.repository;

import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.enums.StatusPagamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByMatriculaId(Long matriculaId);

    boolean existsByMatriculaIdAndStatus(Long matriculaId, StatusPagamento status);
}
