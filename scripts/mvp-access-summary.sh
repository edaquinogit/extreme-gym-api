#!/usr/bin/env bash
set -e

REPORT_DIR="target/surefire-reports"

tests=0
failures=0
errors=0
skipped=0

if [ -d "$REPORT_DIR" ]; then
  while IFS= read -r report; do
    line="$(grep -m 1 "<testsuite" "$report" || true)"
    [ -z "$line" ] && continue

    current_tests="$(printf '%s\n' "$line" | sed -n 's/.* tests="\([0-9][0-9]*\)".*/\1/p')"
    current_failures="$(printf '%s\n' "$line" | sed -n 's/.* failures="\([0-9][0-9]*\)".*/\1/p')"
    current_errors="$(printf '%s\n' "$line" | sed -n 's/.* errors="\([0-9][0-9]*\)".*/\1/p')"
    current_skipped="$(printf '%s\n' "$line" | sed -n 's/.* skipped="\([0-9][0-9]*\)".*/\1/p')"

    tests=$((tests + ${current_tests:-0}))
    failures=$((failures + ${current_failures:-0}))
    errors=$((errors + ${current_errors:-0}))
    skipped=$((skipped + ${current_skipped:-0}))
  done < <(find "$REPORT_DIR" -name "TEST-*.xml" -type f | sort)
fi

if [ "$tests" -gt 0 ] && [ "$failures" -eq 0 ] && [ "$errors" -eq 0 ]; then
  build_status="BUILD SUCCESS"
else
  build_status="BUILD NOT VERIFIED"
fi

cat <<SUMMARY
Extreme Gym API - Fluxo MVP de Acesso
Java 21 | Spring Boot | API REST | Regras de Negocio

Fluxo principal:
Aluno -> Plano -> Matricula -> Pagamento -> Validacao de Acesso -> Check-in

Regras consideradas:
- aluno deve estar ativo
- matricula deve estar ativa
- plano deve existir e estar ativo para matricula
- pagamento deve estar valido/pago
- acesso pode ser liberado ou bloqueado
- check-in registra o resultado da validacao

Classes principais:
- AlunoService
- PlanoService
- MatriculaService
- PagamentoService
- AcessoService
- CheckInService

Validacao:
Tests run: $tests, Failures: $failures, Errors: $errors, Skipped: $skipped
$build_status
SUMMARY
