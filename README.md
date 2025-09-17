# RielHome Automation Platform

RielHome is a modular Spring Boot 3 service that orchestrates Instagram business messaging, live chat, and tenant-aware automation flows. The platform is implemented as a modular monolith backed by PostgreSQL 15 with Flyway migrations and extensive auditing.

## Project structure

The Maven build is composed of a Boot application module and a family of domain-focused libraries:

| Module | Purpose |
| ------ | ------- |
| `app` | Spring Boot WebFlux application with security, persistence, and Flyway bootstrap. |
| `modules/infra` | Shared infrastructure pieces such as the audited JPA base entity and repository abstractions. |
| `modules/auth` | Core authentication aggregates (users, refresh tokens, email verification, password reset). |
| `modules/tenancy` | Multi-tenant ownership model with organizations and memberships. |
| Remaining modules | Placeholders for email delivery, Instagram integration, webhooks, flows, engine, scheduler, live chat, and analytics. |

Each module is published as a regular Maven `jar` and consumed by the `app` module, enabling explicit dependency wiring between bounded contexts.

## Getting started

### Prerequisites

- Java 21
- Docker (required for Testcontainers-backed integration tests)

### Useful commands

```bash
./mvnw clean verify          # compile modules and execute unit/integration tests
./mvnw spring-boot:run       # start the reactive HTTP server on port 8080
```

The Maven wrapper downloads its supporting JAR and Maven 3.9.9 distribution from Maven Central on first use, keeping binary
artifacts out of version control.

During test execution a PostgreSQL 15 container is launched automatically. Migrations located under `app/src/main/resources/db/migration` are applied via Flyway before repositories interact with the schema.

### Configuration

The application relies on environment variables to describe external services:

- `DB_URL`, `DB_USER`, `DB_PASS` – PostgreSQL connectivity
- `APP_BASE_URL` – public hostname used for templating verification and reset links
- `JWT_SECRET`, `ACCESS_TOKEN_TTL`, `REFRESH_TOKEN_TTL` – authentication token configuration (HS512 secret, access TTL, refresh TTL)
- `VERIFICATION_TOKEN_TTL`, `PASSWORD_RESET_TTL`, `PASSWORD_ENCODER` – optional overrides for verification/reset expirations and Argon2/Bcrypt selection
- `EMAIL_FROM`, `EMAIL_VERIFICATION_BASE_URL`, `EMAIL_RESET_BASE_URL` – outbound email metadata for verification/reset flows
- `EMAIL_PROVIDER`, provider-specific credentials, and Meta/Google OAuth secrets (future milestones)

Default fallback values exist for local development (see `application.yml`).

### Auth & tenancy capabilities

- **Registration** – `POST /api/auth/register` creates a user, seeds a dedicated tenant, assigns the Owner membership, and dispatches an email verification link.
- **Login** – `POST /api/auth/login` verifies credentials using Argon2id (or Bcrypt via configuration), enforces email verification, and issues a JWT HS512 access token alongside a persisted refresh token containing IP/User-Agent metadata.
- **Refresh** – `POST /api/auth/token/refresh` rotates refresh tokens, revoking the previous value before returning a fresh token pair.
- **Verification** – `GET /api/auth/verify-email` toggles the verification flag, while `POST /api/auth/verify-email/resend` regenerates links when required.
- **Password reset** – `POST /api/auth/password/forgot` and `POST /api/auth/password/reset` drive reset flows, revoking existing refresh tokens after a successful password update.
- **Google OAuth** – `GET /api/auth/google/authorize` redirects to Google consent, while `GET /api/auth/google/callback` exchanges the code and issues platform tokens (creating a tenant for first-time Google users).

All authentication endpoints emit structured errors following the `{ code, message, details }` contract documented in `GlobalExceptionHandler`.

## Testing and quality

Testcontainers-backed integration tests cover the full auth lifecycle (register → verify → login → refresh → resend → reset) via `AuthControllerTest`, while the original JPA slice test continues validating audited persistence wiring. All tests run against PostgreSQL 15 inside Docker to mirror production characteristics.

CI/CD and additional checks will be added in subsequent milestones as the feature set grows.

For a chronological list of notable changes refer to [CHANGELOG.md](CHANGELOG.md).
