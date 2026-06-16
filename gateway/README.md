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
2. Configure `BACKEND_BASE_URL`, `DEVICE_ID`, `DEVICE_API_KEY`, `DEVICE_HMAC_SECRET` and `ADMIN_API_KEY`.
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

> Operational endpoints are protected by `x-admin-api-key`. Set `ADMIN_API_KEY` in `.env` and never expose it through the public gateway surface.
>
> CI now validates the gateway package in the existing workflow with build, lint, type-check, test and production audit stages.

## Security / Vulnerability Fix (applied)

Actions taken:

- Upgraded `fastify` to `^5.8.5` to address multiple high-severity CVEs affecting `fast-uri`, `fast-json-stringify` and related transitive packages.
- Performed a clean install and audit: `npm ci` / `npm audit` (result: 0 vulnerabilities in current dependency tree at time of validation).
- Ran full TypeScript validation, build and unit tests: `npm run type-check`, `npm run build`, `npm run test` (all passed locally: 12/12 tests).
- Added ESLint config and fixed or documented warnings (14 warnings remain, all `@typescript-eslint/no-explicit-any`).

Validation summary (local):

- `npm audit`: 0 vulnerabilities after upgrade
- `tsc --noEmit`: OK
- `tsc -p tsconfig.json`: OK (build produced `dist/`)
- `vitest run`: 12/12 tests passed
- `eslint`: 14 warnings, 0 errors

PR checklist (recommended before merging to `main`):

1. Confirm CI pipeline runs `npm ci`, `npm run type-check`, `npm run lint`, `npm run build`, `npm run test`.
2. Run `npm audit --production` in CI to confirm production dependency surface has no known high/critical vulns.
3. Do a quick functional smoke test on a staging environment (snapshot fetch, validate access, event sync).
4. Validate runtime HTTP surface under expected load; confirm size/payload limits and header validation are enforced.
5. Ensure secrets are set in CI and production correctly (`DEVICE_API_KEY`, `DEVICE_HMAC_SECRET`, `BACKEND_BASE_URL`, `GATEWAY_ID`).
6. If running behind a proxy or load balancer, ensure untrusted headers are not forwarded or are validated/whitelisted.
7. Add monitoring/alerting for sync failures, DB errors, and heartbeat failures.

Commands used during remediation (repro):

```bash
cd gateway
npm ci
npm audit --json > gateway-audit.json
npm run type-check
npm run build
npm run test
```

Notes:

- The upgrade to Fastify 5.x is a semver-major change; tests and type-checking passed in this codebase but it's recommended to run the gateway in a staging environment and exercise integrations before promoting to production.
- Remaining ESLint warnings relate to explicit `any` usage in boundary code; consider incremental tightening (replace with typed interfaces or `_`-prefixed ignored args) in a follow-up task.

If you want, I will now commit this documentation update with a senior-style commit message and push the change to the current branch.
