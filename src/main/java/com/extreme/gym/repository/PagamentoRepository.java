package com.extreme.gym.repository;

import com.extreme.gym.entity.Pagamento;
import com.extreme.gym.enums.StatusPagamento;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Page<Pagamento> findByMatriculaId(Long matriculaId, Pageable pageable);

    List<Pagamento> findByMatriculaId(Long matriculaId);

    boolean existsByMatriculaIdAndStatus(Long matriculaId, StatusPagamento status);

    @Query("select p from Pagamento p join p.matricula m join m.aluno a " +
            "where lower(a.nome) like lower(concat('%', :termo, '%')) order by p.id desc")
    List<Pagamento> buscarResumoGlobal(@Param("termo") String termo, Pageable pageable);
}
