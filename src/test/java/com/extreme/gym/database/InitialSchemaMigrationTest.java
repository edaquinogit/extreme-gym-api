package com.extreme.gym.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class InitialSchemaMigrationTest {

    private static final Path INITIAL_SCHEMA = Path.of(
            "src/main/resources/db/migration/V1__create_initial_schema.sql"
    );

    @Test
    void migrationInicialDeveConterIndicesUnicosParciaisCriticos() throws Exception {
        String migration = Files.readString(INITIAL_SCHEMA);

        assertThat(migration)
                .contains("create unique index uk_matriculas_aluno_ativa")
                .contains("on matriculas (aluno_id)")
                .contains("where status = 'ATIVA'")
                .contains("create unique index uk_pagamentos_matricula_pago")
                .contains("on pagamentos (matricula_id)")
                .contains("where status = 'PAGO'");
    }
}
