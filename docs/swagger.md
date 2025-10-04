# Swagger / OpenAPI Documentation

## URL
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **Tip:** When the service is deployed behind a reverse proxy, ensure the proxy forwards the `X-Forwarded-*` headers so that Swagger UI can build correct absolute URLs.

## Authentication via Swagger UI
1. Obtain an access token using `/api/auth/login`, `/api/auth/token/refresh`, or `/api/auth/google/callback`.
2. Click the **Authorize** button in the top-right corner of Swagger UI.
3. Enter the token as `Bearer <access_token>` and click **Authorize**.
4. All endpoints that require authentication (e.g. `GET /api/auth/me`) will now include the bearer token automatically when executed from Swagger UI.

## Implementation Best Practices
- **Keep documentation close to code**: Use `@Operation`, `@ApiResponse`, and `@Schema` annotations on controllers and DTOs so that the contract evolves with the implementation.
- **Document security schemes once**: Define bearer token authentication in a central configuration (`OpenApiConfig`) and reference it with `@SecurityRequirement` on protected endpoints.
- **Describe request/response models**: Annotate request records with `@Schema` to highlight constraints, examples, and field descriptions.
- **Expose only necessary endpoints**: Permit Swagger and OpenAPI paths explicitly in the security configuration to avoid exposing other internal routes unintentionally.
- **Version your API**: Update the OpenAPI `Info` metadata (`title`, `version`, `description`) whenever introducing breaking changes.
- **Keep dependencies updated**: Regularly review `springdoc-openapi` for updates to stay compatible with new Spring Boot releases and security patches.
- **Enable WebJar resolution**: Make sure the project depends on `webjars-locator-core` so that `/swagger-ui.html` resolves to the correct versioned Swagger UI assets when running on Spring WebFlux.

