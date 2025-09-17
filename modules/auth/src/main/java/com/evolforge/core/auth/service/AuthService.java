package com.evolforge.core.auth.service;

import com.evolforge.core.auth.config.AuthProperties;
import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.domain.token.EmailVerificationToken;
import com.evolforge.core.auth.domain.token.PasswordResetToken;
import com.evolforge.core.auth.domain.token.RefreshToken;
import com.evolforge.core.auth.exception.AuthException;
import com.evolforge.core.auth.repository.EmailVerificationTokenRepository;
import com.evolforge.core.auth.repository.PasswordResetTokenRepository;
import com.evolforge.core.auth.repository.RefreshTokenRepository;
import com.evolforge.core.auth.repository.UserAccountRepository;
import com.evolforge.core.auth.service.dto.ClientMetadata;
import com.evolforge.core.auth.service.dto.EmailVerificationResult;
import com.evolforge.core.auth.service.dto.GoogleAccountResult;
import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.evolforge.core.auth.service.dto.RegistrationResult;
import com.evolforge.core.auth.service.dto.TokenPair;
import com.evolforge.core.email.EmailProperties;
import com.evolforge.core.email.EmailSender;
import com.evolforge.core.igoauth.GoogleProfile;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;
    private final EmailSender emailSender;
    private final EmailProperties emailProperties;
    private final TokenGenerator tokenGenerator;
    private final JwtService jwtService;
    private final MembershipLookup membershipLookup;

    public AuthService(UserAccountRepository userAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthProperties properties,
            EmailSender emailSender,
            EmailProperties emailProperties,
            TokenGenerator tokenGenerator,
            JwtService jwtService,
            MembershipLookup membershipLookup) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.emailSender = emailSender;
        this.emailProperties = emailProperties;
        this.tokenGenerator = tokenGenerator;
        this.jwtService = jwtService;
        this.membershipLookup = membershipLookup;
    }

    @Transactional
    public RegistrationResult registerAccount(String email, String password, String displayName) {
        String normalizedEmail = normalizeEmail(email);
        userAccountRepository.findByEmail(normalizedEmail)
                .ifPresent(existing -> {
                    throw AuthException.conflict("auth.email_taken", "Account with this email already exists");
                });

        UserAccount user = new UserAccount();
        user.setEmail(normalizedEmail);
        user.setDisplayName(displayName);
        user.setEmailVerified(false);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisabled(false);
        user = userAccountRepository.save(user);

        EmailVerificationToken verificationToken = createEmailVerificationToken(user);
        emailSender.sendVerificationEmail(user.getEmail(), user.getDisplayName(),
                emailProperties.buildVerificationLink(verificationToken.getToken()));

        return new RegistrationResult(user);
    }

    @Transactional
    public TokenPair login(String email, String password, ClientMetadata metadata) {
        UserAccount user = userAccountRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> AuthException.unauthorized("auth.invalid_credentials", "Invalid credentials"));

        if (user.isDisabled()) {
            throw AuthException.forbidden("auth.account_disabled", "Account has been disabled");
        }

        if (!user.isEmailVerified()) {
            throw AuthException.forbidden("auth.email_not_verified", "Email address has not been verified");
        }

        if (!passwordEncoder.matches(password, Objects.toString(user.getPasswordHash(), ""))) {
            throw AuthException.unauthorized("auth.invalid_credentials", "Invalid credentials");
        }

        return issueTokenPair(user, metadata);
    }

    @Transactional
    public TokenPair refresh(String tokenValue, ClientMetadata metadata) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() ->
                        AuthException.unauthorized("auth.refresh_invalid", "Refresh token is invalid or expired"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw AuthException.unauthorized("auth.refresh_invalid", "Refresh token is invalid or expired");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return issueTokenPair(refreshToken.getUser(), metadata);
    }

    @Transactional
    public void revokeRefreshTokensForUser(UserAccount user) {
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        Optional<UserAccount> userOptional = userAccountRepository.findByEmail(normalizeEmail(email));
        if (userOptional.isEmpty()) {
            return;
        }
        UserAccount user = userOptional.get();
        if (user.isEmailVerified()) {
            return;
        }
        EmailVerificationToken verificationToken = createEmailVerificationToken(user);
        emailSender.sendVerificationEmail(user.getEmail(), user.getDisplayName(),
                emailProperties.buildVerificationLink(verificationToken.getToken()));
    }

    @Transactional
    public EmailVerificationResult verifyEmail(String tokenValue) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> AuthException.badRequest("auth.verification_invalid", "Verification token invalid"));

        if (token.isUsed()) {
            return new EmailVerificationResult(token.getUser().isEmailVerified());
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw AuthException.badRequest("auth.verification_expired", "Verification token expired");
        }

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);

        UserAccount user = token.getUser();
        user.setEmailVerified(true);
        userAccountRepository.save(user);

        return new EmailVerificationResult(true);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<UserAccount> userOptional = userAccountRepository.findByEmail(normalizeEmail(email));
        if (userOptional.isEmpty()) {
            return;
        }

        UserAccount user = userOptional.get();
        PasswordResetToken resetToken = createPasswordResetToken(user);
        emailSender.sendPasswordResetEmail(user.getEmail(), user.getDisplayName(),
                emailProperties.buildPasswordResetLink(resetToken.getToken()));
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(tokenValue).orElseThrow(() ->
                AuthException.badRequest("auth.reset_invalid", "Password reset token invalid"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw AuthException.badRequest("auth.reset_invalid", "Password reset token invalid or expired");
        }

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        UserAccount user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);

        revokeRefreshTokensForUser(user);
    }

    @Transactional
    public GoogleAccountResult loginWithGoogle(GoogleProfile profile) {
        if (profile == null || !StringUtils.hasText(profile.email())) {
            throw AuthException.badRequest("auth.google_invalid_profile", "Google profile is missing required fields");
        }

        UserAccount user = userAccountRepository.findByGoogleSub(profile.subject()).orElse(null);
        boolean newAccount = false;
        String normalizedEmail = normalizeEmail(profile.email());

        if (user == null) {
            user = userAccountRepository.findByEmail(normalizedEmail).orElse(null);
            if (user == null) {
                user = new UserAccount();
                user.setEmail(normalizedEmail);
                user.setDisplayName(profile.effectiveDisplayName());
                user.setGoogleSub(profile.subject());
                user.setEmailVerified(profile.emailVerified());
                user.setPasswordHash(null);
                user.setDisabled(false);
                user = userAccountRepository.save(user);
                newAccount = true;
            } else {
                if (user.isDisabled()) {
                    throw AuthException.forbidden("auth.account_disabled", "Account has been disabled");
                }
                user.setGoogleSub(profile.subject());
                if (profile.emailVerified() && !user.isEmailVerified()) {
                    user.setEmailVerified(true);
                }
                if (!StringUtils.hasText(user.getDisplayName())) {
                    user.setDisplayName(profile.effectiveDisplayName());
                }
                user = userAccountRepository.save(user);
            }
        } else {
            if (user.isDisabled()) {
                throw AuthException.forbidden("auth.account_disabled", "Account has been disabled");
            }
            if (profile.emailVerified() && !user.isEmailVerified()) {
                user.setEmailVerified(true);
                user = userAccountRepository.save(user);
            }
            if (!StringUtils.hasText(user.getDisplayName())) {
                user.setDisplayName(profile.effectiveDisplayName());
                user = userAccountRepository.save(user);
            }
        }

        return new GoogleAccountResult(user, newAccount);
    }

    @Transactional
    public TokenPair createSession(UserAccount user, ClientMetadata metadata) {
        return issueTokenPair(user, metadata);
    }

    private TokenPair issueTokenPair(UserAccount user, ClientMetadata metadata) {
        List<MembershipDescriptor> memberships = membershipLookup.membershipsForUser(user.getId());
        JwtService.JwtToken jwtToken = jwtService.generate(user, memberships);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(tokenGenerator.generateToken(48));
        refreshToken.setExpiresAt(Instant.now().plus(properties.getRefreshToken().getTtl()));
        refreshToken.setRevoked(false);
        if (metadata != null) {
            refreshToken.setUserAgent(metadata.userAgent());
            refreshToken.setIp(metadata.ipAddress());
        }
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(jwtToken.token(), jwtToken.expiresAt(), refreshToken.getToken());
    }

    private EmailVerificationToken createEmailVerificationToken(UserAccount user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(tokenGenerator.generateToken(32));
        token.setExpiresAt(Instant.now().plus(properties.getVerification().getTtl()));
        token.setUsed(false);
        return emailVerificationTokenRepository.save(token);
    }

    private PasswordResetToken createPasswordResetToken(UserAccount user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(tokenGenerator.generateToken(32));
        token.setExpiresAt(Instant.now().plus(properties.getPasswordReset().getTtl()));
        token.setUsed(false);
        return passwordResetTokenRepository.save(token);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
