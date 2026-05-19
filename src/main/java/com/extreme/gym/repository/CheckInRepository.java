package com.extreme.gym.repository;

import com.extreme.gym.entity.CheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Page<CheckIn> findByAlunoId(Long alunoId, Pageable pageable);
}
