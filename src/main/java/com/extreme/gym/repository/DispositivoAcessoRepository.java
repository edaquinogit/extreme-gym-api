package com.extreme.gym.repository;

import com.extreme.gym.entity.DispositivoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositivoAcessoRepository extends JpaRepository<DispositivoAcesso, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
