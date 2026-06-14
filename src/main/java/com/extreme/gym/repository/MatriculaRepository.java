package com.extreme.gym.repository;

import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusMatricula;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAlunoIdAndStatus(Long alunoId, StatusMatricula status);

    Optional<Matricula> findByAlunoIdAndStatus(Long alunoId, StatusMatricula status);

    List<Matricula> findByAlunoId(Long alunoId, Pageable pageable);
}
