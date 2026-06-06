package com.extreme.gym.repository;

import com.extreme.gym.entity.DispositivoAcesso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositivoAcessoRepository extends JpaRepository<DispositivoAcesso, Long> {

    Optional<DispositivoAcesso> findByIdentificadorExterno(String identificadorExterno);
}
