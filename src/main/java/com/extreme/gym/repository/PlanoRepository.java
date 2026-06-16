package com.extreme.gym.repository;

import com.extreme.gym.entity.Plano;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanoRepository extends JpaRepository<Plano, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);

    @Query("select p from Plano p where lower(p.nome) like lower(concat('%', :termo, '%')) " +
            "or lower(p.descricao) like lower(concat('%', :termo, '%')) order by p.nome")
    List<Plano> buscarResumoGlobal(@Param("termo") String termo, Pageable pageable);
}
