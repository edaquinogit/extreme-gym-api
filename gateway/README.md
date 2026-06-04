# Extreme Gym Local Access Gateway

Este diretório contém a base para o gateway local desacoplado de fabricantes, responsável por:

- armazenar snapshot local de autorizações;
- validar acessos online/offline;
- registrar eventos locais;
- sincronizar eventos com o backend;
- enviar heartbeat periódico;
- suportar adapters vendors-agnostic.

## Como usar

1. Copie `.env.example` para `.env`.
2. Configure `BACKEND_BASE_URL`, `DEVICE_ID`, `DEVICE_API_KEY` e `DEVICE_HMAC_SECRET`.
3. Execute:

```bash
cd gateway
npm install
npm run build
npm start
```

## Scripts

- `npm run type-check`: validação TypeScript
- `npm run lint`: ESLint
- `npm run build`: compila para `dist`
- `npm run test`: executa testes com Vitest

## Escopo inicial

Este gateway é uma base arquitetural e não implementa integração com hardware de produção. O objetivo é fornecer uma camada local segura que possa ser estendida com adapters de fabricantes reais no futuro.

## Security / Vulnerability Status (automated scan)

- `npm install` added 273 packages locally and initial scan reported 5 high-severity vulnerabilities (see `npm audit` section below).
- `npm audit fix` (safe, non-force) was executed — it applied non-breaking fixes where possible but the remaining issues require a major upgrade of `fastify` and related transitive packages.

Vulnerable packages found:
- `fast-uri` (high) — transitive dependency used by `fast-json-stringify` / `@fastify/*` (path traversal / host confusion advisories).
- `fast-json-stringify` (high) — transitive; affects runtime serialisation used by `fastify`.
- `@fastify/ajv-compiler`, `@fastify/fast-json-stringify-compiler` (high) — transitive.
- `fastify` (high) — direct dependency; remediation requires upgrading to `fastify@^5.8.5` (semver-major).

Impact and mitigation plan:
- These vulnerabilities affect runtime (production) because `fastify` is a production dependency. They should be addressed before deploying to production.
- Recommended next steps:
	1. Evaluate upgrading `fastify` to `^5.8.5` and test the gateway thoroughly (breaking changes expected).
	2. Alternatively, hold upgrade and apply runtime mitigations: restrict untrusted headers (e.g., `X-Forwarded-*`), validate/normalize incoming URLs strictly, set body and payload size limits, and run an application-level WAF where possible.
	3. After upgrade, run full test suite and `npm audit` again; consider CVE tracking and backport fixes.

Commands run during this work:
```bash
cd gateway
npm install
npm audit --json > gateway-audit.json
npm audit fix --json > gateway-audit-fix.json
```

If you want, I can attempt a controlled `fastify` upgrade in a feature branch, run the tests, and report any breaking changes.
