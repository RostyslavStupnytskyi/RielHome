package com.evolforge.core.security;

import com.evolforge.core.RielHomeApplication;
import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.service.JwtService;
import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootTest(
        classes = RielHomeApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration")
@AutoConfigureWebTestClient
class SecurityIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtService jwtService;

    @Test
    void protectedEndpointRequiresAuthentication() {
        webTestClient.get()
                .uri("/api/private/ping")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpointAllowsAuthenticatedAccess() {
        JwtService.JwtToken token = jwtService.generate(sampleUser(), sampleMemberships());

        webTestClient.get()
                .uri("/api/private/ping")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("pong");
    }

    private UserAccount sampleUser() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setDisplayName("Test User");
        return user;
    }

    private List<MembershipDescriptor> sampleMemberships() {
        return List.of(new MembershipDescriptor(UUID.randomUUID(), "owner"));
    }

    @TestConfiguration
    static class ProtectedEndpointConfiguration {

        @Bean
        ProtectedController protectedController() {
            return new ProtectedController();
        }
    }

    @RestController
    static class ProtectedController {

        @GetMapping("/api/private/ping")
        Mono<String> ping(Authentication authentication) {
            return Mono.just("pong");
        }
    }
}
