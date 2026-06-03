package com.extreme.gym.repository;

import com.extreme.gym.entity.EventoAcesso;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoAcessoRepository extends JpaRepository<EventoAcesso, Long> {

    Optional<EventoAcesso> findByIdempotencyKey(String idempotencyKey);

    List<EventoAcesso> findByDataHoraEventoBetweenOrderByDataHoraEventoDesc(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    List<EventoAcesso> findByAlunoIdOrderByDataHoraEventoDesc(Long alunoId);

    List<EventoAcesso> findByDispositivoIdOrderByDataHoraEventoDesc(Long dispositivoId);
}
