# Changelog

## [0.2.1] - 2025-09-18
### Removed
- Deleted committed Maven wrapper binaries and switched the wrapper scripts to download artifacts on demand.
- Pointed the Maven wrapper distribution to Maven Central so the binary archive is no longer vendored in the repo.

## [0.2.0] - 2025-09-18
### Added
- Auth facade that orchestrates user onboarding with tenant creation and owner membership in a single transaction.
- Comprehensive authentication service covering registration, login, refresh token rotation, email verification, and password reset flows backed by JWT HS512 tokens.
- Reactive `/api/auth` controller with validation, structured API errors, and metadata capture for refresh tokens.
- Logging email sender with configurable templates plus integration tests covering register, verify, login, refresh, resend, and password reset scenarios.
- Google OAuth2 integration for login/registration with redirect endpoints and profile exchange.

## [0.1.1] - 2025-09-17
### Changed
- Migrated the build from Gradle to a Maven multi-module layout with wrapper support.
- Renamed the base Java package from `com.rielhome` to `com.evolforge.core` across application, domain modules, and tests.

## [0.1.0] - 2025-09-16
### Added
- Spring Boot 3 modular monolith skeleton with domain modules and Gradle multi-project build.
- PostgreSQL persistence baseline with Flyway migrations and audited base entities.
- Testcontainers-powered JPA repository test covering auth and tenancy aggregates.
