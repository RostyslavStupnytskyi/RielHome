# RielHome Automation Platform

RielHome is a Spring Boot 3 service that orchestrates Instagram business messaging, live chat, and tenant-aware automation flows. The platform is implemented as a cohesive application backed by PostgreSQL 17 with Flyway migrations and extensive auditing.

## Project structure

The Maven build is now a single Spring Boot module rooted at the top-level `src` directory. Packages remain grouped by domain (auth, tenancy, infra, etc.), but they are compiled together which simplifies dependency management and IDE navigation.

## Getting started

### Prerequisites

- Java 21
- Docker (required for Testcontainers-backed integration tests)

### Useful commands

```bash
./mvnw clean verify          # compile the application and execute unit/integration tests
./mvnw spring-boot:run       # start the reactive HTTP server on port 8080
docker compose up -d         # start the local PostgreSQL 17 database
```

The Maven wrapper downloads its supporting JAR and Maven 3.9.9 distribution from Maven Central on first use, keeping binary
artifacts out of version control.

During test execution a PostgreSQL 17 container is launched automatically. Migrations located under `src/main/resources/db/migration` are applied via Flyway before repositories interact with the schema.

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

Testcontainers-backed integration tests cover the full auth lifecycle (register → verify → login → refresh → resend → reset) via `AuthControllerTest`, while the original JPA slice test continues validating audited persistence wiring. All tests run against PostgreSQL 17 inside Docker to mirror production characteristics.

CI/CD and additional checks will be added in subsequent milestones as the feature set grows.

For a chronological list of notable changes refer to [CHANGELOG.md](CHANGELOG.md).
