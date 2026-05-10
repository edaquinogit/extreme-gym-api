package com.extreme.gym.repository;

import com.extreme.gym.entity.CheckIn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByAlunoId(Long alunoId);
}
