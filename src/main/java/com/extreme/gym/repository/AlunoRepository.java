package com.extreme.gym.repository;
import com.extreme.gym.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    @Query("select case when count(a) > 0 then true else false end from Aluno a " +
            "where a.email = :email and a.status <> com.extreme.gym.enums.StatusAluno.INATIVO")
    boolean existsByEmail(@Param("email") String email);

    @Query("select case when count(a) > 0 then true else false end from Aluno a " +
            "where a.email = :email and a.id <> :id and a.status <> com.extreme.gym.enums.StatusAluno.INATIVO")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Override
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM alunos", nativeQuery = true)
    void deleteAll();
}
