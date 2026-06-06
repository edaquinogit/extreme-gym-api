package com.extreme.gym.repository;

import com.extreme.gym.entity.CredencialAcesso;
import com.extreme.gym.enums.StatusCredencialAcesso;
import com.extreme.gym.enums.TipoCredencialAcesso;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredencialAcessoRepository extends JpaRepository<CredencialAcesso, Long> {

    List<CredencialAcesso> findByAlunoId(Long alunoId);

    List<CredencialAcesso> findByStatus(StatusCredencialAcesso status);

    Optional<CredencialAcesso> findByTipoAndIdentificadorExterno(
            TipoCredencialAcesso tipo,
            String identificadorExterno
    );

    boolean existsByTipoAndIdentificadorExterno(TipoCredencialAcesso tipo, String identificadorExterno);
}
