package com.extreme.gym.repository;

import com.extreme.gym.entity.EventoAcesso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoAcessoRepository extends JpaRepository<EventoAcesso, Long> {

    Optional<EventoAcesso> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
