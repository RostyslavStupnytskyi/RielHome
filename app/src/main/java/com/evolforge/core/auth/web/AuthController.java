package com.evolforge.core.auth.web;

import com.evolforge.core.auth.AuthFacade;
import com.evolforge.core.auth.AuthFacade.RegistrationResponse;
import com.evolforge.core.auth.service.dto.ClientMetadata;
import com.evolforge.core.auth.service.dto.TokenPair;
import com.evolforge.core.igoauth.GoogleOAuthResult;
import com.evolforge.core.igoauth.GoogleOAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class AuthController {

    private final AuthFacade authFacade;
    private final GoogleOAuthService googleOAuthService;

    public AuthController(AuthFacade authFacade, GoogleOAuthService googleOAuthService) {
        this.authFacade = authFacade;
        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<RegistrationResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return Mono.fromCallable(() -> authFacade.register(request.email(), request.password(), request.displayName()))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TokenResponse> login(@Valid @RequestBody LoginRequest request, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> authFacade.login(request.email(), request.password(),
                        extractMetadata(exchange)))
                .map(AuthController::toTokenResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/token/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> authFacade.refresh(request.refreshToken(), extractMetadata(exchange)))
                .map(AuthController::toTokenResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/verify-email/resend", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return Mono.fromRunnable(() -> authFacade.resendVerificationEmail(request.email()))
                .thenReturn(ResponseEntity.noContent().build())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = "/verify-email")
    public Mono<EmailVerificationResponse> verifyEmail(@RequestParam("token") String token) {
        return Mono.fromCallable(() -> authFacade.verifyEmail(token))
                .map(result -> new EmailVerificationResponse(result.verified()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/password/forgot", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return Mono.fromRunnable(() -> authFacade.requestPasswordReset(request.email()))
                .thenReturn(ResponseEntity.noContent().build())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/password/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return Mono.fromRunnable(() -> authFacade.resetPassword(request.token(), request.newPassword()))
                .thenReturn(ResponseEntity.noContent().build())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = "/google/authorize")
    public Mono<ResponseEntity<Void>> googleAuthorize(@RequestParam(value = "state", required = false) String state) {
        return Mono.fromCallable(() -> googleOAuthService.buildAuthorizationUrl(state))
                .map(url -> ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = "/google/callback")
    public Mono<TokenResponse> googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            ServerWebExchange exchange) {
        return Mono.fromCallable(() -> googleOAuthService.exchangeCode(code))
                .map(GoogleOAuthResult::profile)
                .map(profile -> authFacade.loginWithGoogle(profile, extractMetadata(exchange)))
                .map(AuthController::toTokenResponse)
                .subscribeOn(Schedulers.boundedElastic());
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

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 100) String displayName) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record ResendVerificationRequest(@Email @NotBlank String email) {
    }

    public record EmailVerificationResponse(boolean verified) {
    }

    public record ForgotPasswordRequest(@Email @NotBlank String email) {
    }

    public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 8, max = 72) String newPassword) {
    }

    public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {
    }
}
