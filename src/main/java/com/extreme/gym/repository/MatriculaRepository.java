package com.extreme.gym.repository;

import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusMatricula;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAlunoIdAndStatus(Long alunoId, StatusMatricula status);

    Optional<Matricula> findByAlunoIdAndStatus(Long alunoId, StatusMatricula status);

    @Query("select m from Matricula m join m.aluno a join m.plano p " +
            "where lower(a.nome) like lower(concat('%', :termo, '%')) " +
            "or lower(p.nome) like lower(concat('%', :termo, '%')) order by m.id desc")
    List<Matricula> buscarResumoGlobal(@Param("termo") String termo, Pageable pageable);
}
