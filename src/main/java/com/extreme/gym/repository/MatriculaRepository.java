package com.extreme.gym.repository;

import com.extreme.gym.entity.Matricula;
import com.extreme.gym.enums.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAlunoIdAndStatus(Long alunoId, StatusMatricula status);
}
