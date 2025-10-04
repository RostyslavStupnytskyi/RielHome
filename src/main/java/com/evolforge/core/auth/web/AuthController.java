package com.evolforge.core.auth.web;

import com.evolforge.core.auth.AuthFacade;
import com.evolforge.core.auth.AuthFacade.CurrentUser;
import com.evolforge.core.auth.AuthFacade.RegistrationResponse;
import com.evolforge.core.auth.exception.AuthException;
import com.evolforge.core.auth.service.dto.ClientMetadata;
import com.evolforge.core.auth.service.dto.TokenPair;
import com.evolforge.core.igoauth.GoogleOAuthResult;
import com.evolforge.core.igoauth.GoogleOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Authentication", description = "Authentication, registration, and identity endpoints")
public class AuthController {

    private final AuthFacade authFacade;
    private final GoogleOAuthService googleOAuthService;

    public AuthController(AuthFacade authFacade, GoogleOAuthService googleOAuthService) {
        this.authFacade = authFacade;
        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Register a new account",
            description = "Creates a new user account and sends an email verification link.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    public Mono<ResponseEntity<RegistrationResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return Mono.fromCallable(() -> authFacade.register(request.email(), request.password(), request.displayName()))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Authenticate with email and password",
            description = "Authenticates the user and returns a JWT access and refresh token pair.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication succeeded"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    public Mono<TokenResponse> login(@Valid @RequestBody LoginRequest request, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> authFacade.login(request.email(), request.password(),
                        extractMetadata(exchange)))
                .map(AuthController::toTokenResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/token/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Refresh an access token",
            description = "Exchanges a valid refresh token for a new access token and refresh token pair.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New tokens issued"),
        @ApiResponse(responseCode = "401", description = "Refresh token is invalid", content = @Content)
    })
    public Mono<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> authFacade.refresh(request.refreshToken(), extractMetadata(exchange)))
                .map(AuthController::toTokenResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Invalidate a refresh token",
            description = "Revokes the provided refresh token so it can no longer be used.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Refresh token revoked"),
        @ApiResponse(responseCode = "400", description = "Refresh token is missing or invalid", content = @Content)
    })
    public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        return Mono.fromRunnable(() -> authFacade.logout(request.refreshToken()))
                .then(Mono.fromSupplier(AuthController::noContentResponse))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/verify-email/resend", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Resend an email verification link",
            description = "Sends a new email verification link to the provided email address.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Verification email sent"),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    public Mono<ResponseEntity<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return Mono.fromRunnable(() -> authFacade.resendVerificationEmail(request.email()))
                .then(Mono.fromSupplier(AuthController::noContentResponse))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = "/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Validates a verification token that was sent via email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email verification result returned"),
        @ApiResponse(responseCode = "400", description = "Verification token is invalid", content = @Content)
    })
    public Mono<EmailVerificationResponse> verifyEmail(@RequestParam("token") String token) {
        return Mono.fromCallable(() -> authFacade.verifyEmail(token))
                .map(result -> new EmailVerificationResponse(result.verified()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/password/forgot", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Start password reset flow",
            description = "Generates a password reset email for the specified account.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reset email sent"),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    public Mono<ResponseEntity<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return Mono.fromRunnable(() -> authFacade.requestPasswordReset(request.email()))
                .then(Mono.fromSupplier(AuthController::noContentResponse))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/password/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Reset password",
            description = "Updates the account password using a valid reset token.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password updated"),
        @ApiResponse(responseCode = "400", description = "Reset token is invalid", content = @Content)
    })
    public Mono<ResponseEntity<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return Mono.fromRunnable(() -> authFacade.resetPassword(request.token(), request.newPassword()))
                .then(Mono.fromSupplier(AuthController::noContentResponse))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = "/google/authorize")
    @Operation(
            summary = "Build Google OAuth authorization URL",
            description = "Returns an HTTP redirect to Google's OAuth authorization endpoint.")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to Google OAuth"),
        @ApiResponse(responseCode = "500", description = "Failed to build the authorization URL", content = @Content)
    })
    public Mono<ResponseEntity<Void>> googleAuthorize(@Parameter(description = "Opaque state parameter to be returned by Google", required = false)
            @RequestParam(value = "state", required = false) String state) {
        return Mono.fromCallable(() -> googleOAuthService.buildAuthorizationUrl(state))
                .map(url -> ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).<Void>build())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = "/google/callback")
    @Operation(
            summary = "Handle Google OAuth callback",
            description = "Exchanges an authorization code issued by Google for a token pair and logs the user in.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login succeeded"),
        @ApiResponse(responseCode = "400", description = "Invalid authorization code", content = @Content)
    })
    public Mono<TokenResponse> googleCallback(
            @Parameter(description = "Authorization code issued by Google") @RequestParam("code") String code,
            @Parameter(description = "Opaque state returned by Google", required = false)
            @RequestParam(value = "state", required = false) String state,
            ServerWebExchange exchange) {
        return Mono.fromCallable(() -> googleOAuthService.exchangeCode(code))
                .map(GoogleOAuthResult::profile)
                .map(profile -> authFacade.loginWithGoogle(profile, extractMetadata(exchange)))
                .map(AuthController::toTokenResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = "/me")
    @Operation(
            summary = "Fetch the current authenticated user",
            description = "Returns profile information and tenant memberships for the authenticated user.",
            security = {@SecurityRequirement(name = "BearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User details returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content)
    })
    public Mono<CurrentUserResponse> currentUser(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> authFacade.currentUser(extractAccessToken(exchange)))
                .map(AuthController::toCurrentUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static ResponseEntity<Void> noContentResponse() {
        return ResponseEntity.noContent().build();
    }

    private static TokenResponse toTokenResponse(TokenPair pair) {
        long expiresIn = Math.max(0, pair.accessTokenExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
        return new TokenResponse(pair.accessToken(), pair.refreshToken(), expiresIn);
    }

    private ClientMetadata extractMetadata(ServerWebExchange exchange) {
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        List<String> forwardedFor = exchange.getRequest().getHeaders().get("X-Forwarded-For");
        String ipAddress = null;
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            ipAddress = forwardedFor.get(0);
        }
        if (ipAddress == null) {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null) {
                if (remoteAddress.getAddress() != null) {
                    ipAddress = remoteAddress.getAddress().getHostAddress();
                } else {
                    ipAddress = remoteAddress.getHostString();
                }
            }
        }
        return new ClientMetadata(ipAddress, userAgent);
    }

    private String extractAccessToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }
        return authorization.substring(7);
    }

    public record RegisterRequest(
            @Email @NotBlank @Schema(description = "User email address", example = "user@example.com") String email,
            @NotBlank @Size(min = 8, max = 72) @Schema(description = "Password (8-72 characters)") String password,
            @NotBlank @Size(max = 100) @Schema(description = "Display name shown to other users") String displayName) {
    }

    public record LoginRequest(
            @Email @NotBlank @Schema(description = "User email address", example = "user@example.com") String email,
            @NotBlank @Schema(description = "Password") String password) {
    }

    public record RefreshRequest(@NotBlank @Schema(description = "Refresh token issued during login") String refreshToken) {
    }

    public record LogoutRequest(@NotBlank @Schema(description = "Refresh token to revoke") String refreshToken) {
    }

    public record ResendVerificationRequest(
            @Email @NotBlank @Schema(description = "Email that should receive the verification link") String email) {
    }

    public record EmailVerificationResponse(
            @Schema(description = "Whether the email was successfully verified") boolean verified) {
    }

    public record ForgotPasswordRequest(
            @Email @NotBlank @Schema(description = "Email that should receive the reset instructions") String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank @Schema(description = "Password reset token sent via email") String token,
            @NotBlank @Size(min = 8, max = 72) @Schema(description = "New password (8-72 characters)") String newPassword) {
    }

    public record TokenResponse(
            @Schema(description = "JWT access token to be used in the Authorization header") String accessToken,
            @Schema(description = "Refresh token used to renew access") String refreshToken,
            @Schema(description = "Seconds until the access token expires") long expiresIn) {
    }

    public record CurrentUserResponse(
            @Schema(description = "Unique user identifier") java.util.UUID id,
            @Schema(description = "User email address") String email,
            @Schema(description = "Display name of the user") String displayName,
            @Schema(description = "Tenant memberships the user belongs to") java.util.List<MembershipResponse> memberships) {
    }

    public record MembershipResponse(
            @Schema(description = "Tenant identifier") java.util.UUID tenantId,
            @Schema(description = "Role granted within the tenant") String role) {
    }

    private static CurrentUserResponse toCurrentUserResponse(CurrentUser currentUser) {
        return new CurrentUserResponse(
                currentUser.id(),
                currentUser.email(),
                currentUser.displayName(),
                currentUser.memberships().stream()
                        .map(membership -> new MembershipResponse(membership.tenantId(), membership.role()))
                        .toList());
    }
}
