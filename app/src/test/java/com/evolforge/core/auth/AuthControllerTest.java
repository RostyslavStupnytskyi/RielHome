package com.evolforge.core.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.evolforge.core.api.ApiError;
import com.evolforge.core.auth.AuthFacade.RegistrationResponse;
import com.evolforge.core.auth.domain.token.EmailVerificationToken;
import com.evolforge.core.auth.domain.token.PasswordResetToken;
import com.evolforge.core.auth.domain.token.RefreshToken;
import com.evolforge.core.auth.repository.EmailVerificationTokenRepository;
import com.evolforge.core.auth.repository.PasswordResetTokenRepository;
import com.evolforge.core.auth.repository.RefreshTokenRepository;
import com.evolforge.core.auth.repository.UserAccountRepository;
import com.evolforge.core.auth.web.AuthController.TokenResponse;
import com.evolforge.core.email.EmailSender;
import com.evolforge.core.tenancy.repository.MembershipRepository;
import com.evolforge.core.tenancy.repository.TenantRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AuthControllerTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RecordingEmailSender emailSender;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE membership, refresh_token, email_verification_token, password_reset_token, tenant, user_account RESTART IDENTITY CASCADE");
        emailSender.clear();
    }

    @Test
    void registrationRequiresVerificationBeforeLogin() {
        RegistrationResponse registration = register("owner@example.com", "Sup3rSecure!", "Owner");
        assertThat(registration.userId()).isNotNull();
        assertThat(registration.tenantId()).isNotNull();

        assertThat(userAccountRepository.findAll()).hasSize(1);
        assertThat(tenantRepository.findAll()).hasSize(1);
        assertThat(membershipRepository.findAll()).hasSize(1);
        assertThat(emailVerificationTokenRepository.findAll()).hasSize(1);
        assertThat(emailSender.getVerificationLinks()).hasSize(1);

        ApiError error = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "owner@example.com", "password", "Sup3rSecure!"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ApiError.class)
                .returnResult()
                .getResponseBody();

        assertThat(error).isNotNull();
        assertThat(error.code()).isEqualTo("auth.email_not_verified");

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findAll().get(0);
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/auth/verify-email")
                        .queryParam("token", verificationToken.getToken())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.verified").isEqualTo(true);

        TokenResponse loginResponse = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "owner@example.com", "password", "Sup3rSecure!"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();
        assertThat(loginResponse.expiresIn()).isPositive();
    }

    @Test
    void refreshRotatesTokens() {
        registerAndVerify("refresh@example.com");

        TokenResponse login = login("refresh@example.com", "Sup3rSecure!");
        assertThat(refreshTokenRepository.findAll()).hasSize(1);

        TokenResponse refreshed = webTestClient.post()
                .uri("/api/auth/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("refreshToken", login.refreshToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(refreshed).isNotNull();
        assertThat(refreshed.refreshToken()).isNotEqualTo(login.refreshToken());

        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        assertThat(tokens).hasSize(2);
        RefreshToken original = tokens.stream()
                .filter(token -> token.getToken().equals(login.refreshToken()))
                .findFirst()
                .orElseThrow();
        assertThat(original.isRevoked()).isTrue();
    }

    @Test
    void resendVerificationCreatesNewToken() {
        register("resend@example.com", "Sup3rSecure!", "Owner");
        assertThat(emailSender.verificationLinks).hasSize(1);

        webTestClient.post()
                .uri("/api/auth/verify-email/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "resend@example.com"))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(emailSender.getVerificationLinks()).hasSize(2);
        assertThat(emailVerificationTokenRepository.findAll()).hasSize(2);
    }

    @Test
    void passwordResetChangesPasswordAndRevokesTokens() {
        registerAndVerify("reset@example.com");
        TokenResponse login = login("reset@example.com", "Sup3rSecure!");
        assertThat(refreshTokenRepository.findAll()).hasSize(1);

        webTestClient.post()
                .uri("/api/auth/password/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "reset@example.com"))
                .exchange()
                .expectStatus().isNoContent();

        PasswordResetToken resetToken = passwordResetTokenRepository.findAll().get(0);

        webTestClient.post()
                .uri("/api/auth/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("token", resetToken.getToken(), "newPassword", "NewPassw0rd!"))
                .exchange()
                .expectStatus().isNoContent();

        ApiError error = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "reset@example.com", "password", "Sup3rSecure!"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ApiError.class)
                .returnResult()
                .getResponseBody();

        assertThat(error).isNotNull();
        assertThat(error.code()).isEqualTo("auth.invalid_credentials");

        TokenResponse newLogin = login("reset@example.com", "NewPassw0rd!");
        assertThat(newLogin).isNotNull();

        assertThat(refreshTokenRepository.findAll()).hasSize(1);
        RefreshToken storedToken = refreshTokenRepository.findAll().get(0);
        assertThat(storedToken.getToken()).isEqualTo(newLogin.refreshToken());
        assertThat(storedToken.getCreatedAt()).isBeforeOrEqualsTo(Instant.now());
    }

    private RegistrationResponse register(String email, String password, String displayName) {
        return webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email, "password", password, "displayName", displayName))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RegistrationResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void registerAndVerify(String email) {
        register(email, "Sup3rSecure!", "Owner");
        EmailVerificationToken token = emailVerificationTokenRepository.findAll().get(0);
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/auth/verify-email")
                        .queryParam("token", token.getToken())
                        .build())
                .exchange()
                .expectStatus().isOk();
        emailVerificationTokenRepository.deleteAll();
    }

    private TokenResponse login(String email, String password) {
        return webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email, "password", password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @TestConfiguration
    static class EmailTestConfiguration {

        @Bean
        @Primary
        RecordingEmailSender recordingEmailSender() {
            return new RecordingEmailSender();
        }
    }

    static class RecordingEmailSender implements EmailSender {

        private final List<String> verificationLinks = new CopyOnWriteArrayList<>();
        private final List<String> resetLinks = new CopyOnWriteArrayList<>();

        RecordingEmailSender() {}

        @Override
        public void sendVerificationEmail(String to, String displayName, String verificationLink) {
            verificationLinks.add(verificationLink);
        }

        @Override
        public void sendPasswordResetEmail(String to, String displayName, String resetLink) {
            resetLinks.add(resetLink);
        }

        void clear() {
            verificationLinks.clear();
            resetLinks.clear();
        }

        List<String> getVerificationLinks() {
            return verificationLinks;
        }

        List<String> getResetLinks() {
            return resetLinks;
        }
    }
}
