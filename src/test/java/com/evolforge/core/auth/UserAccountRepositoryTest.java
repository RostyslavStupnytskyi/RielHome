package com.evolforge.core.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.domain.token.EmailVerificationToken;
import com.evolforge.core.auth.domain.token.PasswordResetToken;
import com.evolforge.core.auth.domain.token.RefreshToken;
import com.evolforge.core.auth.repository.EmailVerificationTokenRepository;
import com.evolforge.core.auth.repository.PasswordResetTokenRepository;
import com.evolforge.core.auth.repository.RefreshTokenRepository;
import com.evolforge.core.auth.repository.UserAccountRepository;
import com.evolforge.core.infra.config.JpaConfig;
import com.evolforge.core.tenancy.domain.Membership;
import com.evolforge.core.tenancy.domain.MembershipRole;
import com.evolforge.core.tenancy.domain.Tenant;
import com.evolforge.core.tenancy.repository.MembershipRepository;
import com.evolforge.core.tenancy.repository.TenantRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class UserAccountRepositoryTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void persistsUserAndRelatedEntities() {
        UserAccount user = new UserAccount();
        user.setEmail("Test@Example.com");
        user.setDisplayName("Test User");
        user.setEmailVerified(false);
        user.setPasswordHash("argon2:hash");
        user.setDisabled(false);

        user = userAccountRepository.saveAndFlush(user);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();

        Tenant tenant = new Tenant();
        tenant.setName("Acme Inc");
        tenant = tenantRepository.saveAndFlush(tenant);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setTenant(tenant);
        membership.setRole(MembershipRole.OWNER);
        membershipRepository.saveAndFlush(membership);

        assertThat(membershipRepository.findByUserId(user.getId())).hasSize(1);
        assertThat(membershipRepository.findByUserIdAndTenantId(user.getId(), tenant.getId())).isPresent();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken("refresh-token");
        refreshToken.setExpiresAt(Instant.now().plus(Duration.ofDays(30)));
        refreshToken.setRevoked(false);
        refreshToken.setUserAgent("JUnit");
        refreshToken.setIp("127.0.0.1");
        refreshTokenRepository.saveAndFlush(refreshToken);

        assertThat(refreshTokenRepository.findByToken("refresh-token")).isPresent();

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken("verify-token");
        verificationToken.setExpiresAt(Instant.now().plus(Duration.ofHours(24)));
        verificationToken.setUsed(false);
        emailVerificationTokenRepository.saveAndFlush(verificationToken);

        assertThat(emailVerificationTokenRepository.findByToken("verify-token")).isPresent();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken("reset-token");
        resetToken.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        resetToken.setUsed(false);
        passwordResetTokenRepository.saveAndFlush(resetToken);

        assertThat(passwordResetTokenRepository.findByToken("reset-token")).isPresent();
    }
}
