# RielHome API Endpoints

> High-signal reference for `/api/auth` routes exposed by the RielHome backend. Optimized for LLM prompts: each endpoint block spells out request contracts, responses, and notable error semantics.

## Base Configuration
- **Base path:** `/api/auth`
- **Transport:** HTTPS recommended; examples omit scheme/host.
- **Media types:** Endpoints consume/produce `application/json` unless stated otherwise.
- **Authentication:** Bearer access tokens (JWT) issued by the auth service. Endpoints explicitly list when a token is required.
- **Error envelope:** All non-2xx responses use [`ApiError`](../src/main/java/com/evolforge/core/api/ApiError.java) with fields `code`, `message`, `details` (map, optional), and `timestamp` (ISO-8601). Global handler mappings:
  - `AuthException` → preserves service-defined status (401/403/409/etc.).
  - Validation failures → `422 Unprocessable Entity`, `code="validation_error"`, `details` keyed by field path.
  - `ResponseStatusException` → status from exception, `code="http_<status>`".
  - Unhandled errors → `500 Internal Server Error`, `code="internal_error"`.

## Shared Data Shapes
- **TokenResponse** (`POST /login`, `POST /token/refresh`, `GET /google/callback`):
  ```json
  {
    "accessToken": "<jwt>",
    "refreshToken": "<opaque token>",
    "expiresIn": 3600 // seconds until access token expiry (derived from JWT exp)
  }
  ```
- **CurrentUserResponse** (`GET /me`):
  ```json
  {
    "id": "<uuid>",
    "email": "user@example.com",
    "displayName": "Jane Doe",
    "memberships": [
      {
        "tenantId": "<uuid>",
        "role": "OWNER"
      }
    ]
  }
  ```

## Account Lifecycle

### `POST /api/auth/register`
Create a password-based user and default tenant workspace.

**Request JSON**
```json
{
  "email": "user@example.com",      // required, valid email
  "password": "<8-72 chars>",      // required, length 8-72
  "displayName": "Jane Doe"        // required, 1-100 chars
}
```

**Responses**
- `201 Created`
  ```json
  {
    "userId": "<uuid>",
    "tenantId": "<uuid>"
  }
  ```

**Error semantics**
- `409 Conflict`, `code="auth.email_taken"` when email already registered.
- Standard `422` for invalid payload (missing/invalid email, short password, blank display name).

### `POST /api/auth/verify-email/resend`
Request another verification email.

**Request JSON**
```json
{
  "email": "user@example.com" // required, valid email
}
```

**Responses**
- `204 No Content` regardless of whether the email exists or is already verified.

**Error semantics**
- `422` validation error for malformed email.

### `GET /api/auth/verify-email`
Mark a verification token as used and flag the account as verified.

**Query params**
- `token` (string, required)

**Responses**
- `200 OK`
  ```json
  {
    "verified": true
  }
  ```
  - `verified=false` if token already consumed but account remained unverified.

**Error semantics**
- `400 Bad Request`, `code="auth.verification_invalid"` for unknown token.
- `400 Bad Request`, `code="auth.verification_expired"` for expired token.

## Session & Token Management

### `POST /api/auth/login`
Exchange email/password credentials for a session.

**Request JSON**
```json
{
  "email": "user@example.com", // required, valid email
  "password": "secret"         // required, non-blank
}
```

**Responses**
- `200 OK` with `TokenResponse`.

**Headers/metadata**
- `User-Agent` (optional) and first value of `X-Forwarded-For` populate session client metadata; falls back to socket IP.

**Error semantics**
- `401 Unauthorized`, `code="auth.invalid_credentials"` for unknown email or wrong password.
- `403 Forbidden`, `code="auth.account_disabled"` when account is disabled.
- `403 Forbidden`, `code="auth.email_not_verified"` when email verification pending.
- `422` for validation failures.

### `POST /api/auth/token/refresh`
Issue a fresh token pair and revoke the supplied refresh token.

**Request JSON**
```json
{
  "refreshToken": "<opaque token>" // required, non-blank
}
```

**Responses**
- `200 OK` with `TokenResponse`.

**Headers/metadata**
- Same metadata extraction as login.

**Error semantics**
- `401 Unauthorized`, `code="auth.refresh_invalid"` for missing, revoked, or expired token.
- `422` for validation failures.

### `POST /api/auth/logout`
Revoke a specific refresh token.

**Request JSON**
```json
{
  "refreshToken": "<opaque token>" // required, non-blank
}
```

**Responses**
- `204 No Content` (idempotent; no error if token already revoked or unknown).

**Error semantics**
- `422` for validation failures.

## Password Recovery

### `POST /api/auth/password/forgot`
Trigger password reset email.

**Request JSON**
```json
{
  "email": "user@example.com" // required, valid email
}
```

**Responses**
- `204 No Content` even if email not found (prevents account enumeration).

**Error semantics**
- `422` for validation failures.

### `POST /api/auth/password/reset`
Finalize password reset using the emailed token.

**Request JSON**
```json
{
  "token": "<reset token>",
  "newPassword": "<8-72 chars>"
}
```

**Responses**
- `204 No Content`. Existing refresh tokens for the user are revoked.

**Error semantics**
- `400 Bad Request`, `code="auth.reset_invalid"` for unknown, used, or expired token.
- `422` for validation failures (blank token or password length outside 8-72).

## Google OAuth

### `GET /api/auth/google/authorize`
Return a redirect to Google OAuth consent page.

**Query params**
- `state` (optional, opaque string) → echoed back by Google.

**Responses**
- `302 Found` with `Location` header set to Google authorization URL. No response body.

### `GET /api/auth/google/callback`
Process Google OAuth callback and create/login the user.

**Query params**
- `code` (required) — authorization code from Google.
- `state` (optional) — forwarded value from `/google/authorize`.

**Responses**
- `200 OK` with `TokenResponse`.

**Headers/metadata**
- Same metadata extraction as login/refresh (for refresh token client metadata).

**Error semantics**
- `400 Bad Request`, `code="auth.google_invalid_profile"` when Google profile lacks required fields.
- `403 Forbidden`, `code="auth.account_disabled"` if linked account disabled.

## Current User

### `GET /api/auth/me`
Retrieve the authenticated account plus tenant memberships.

**Headers**
- `Authorization: Bearer <access token>` (required)

**Responses**
- `200 OK` with `CurrentUserResponse`.

**Error semantics**
- `401 Unauthorized`, `code="auth.access_invalid"` when bearer token missing, malformed, expired, or refers to a non-existent user.
- `403 Forbidden`, `code="auth.account_disabled"` if the account is disabled between session issuance and fetch.
