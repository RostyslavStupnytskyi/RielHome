package com.evolforge.core.auth;

import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.service.AuthService;
import com.evolforge.core.auth.service.dto.ClientMetadata;
import com.evolforge.core.auth.service.dto.EmailVerificationResult;
import com.evolforge.core.auth.service.dto.GoogleAccountResult;
import com.evolforge.core.auth.service.dto.RegistrationResult;
import com.evolforge.core.auth.service.dto.TokenPair;
import com.evolforge.core.igoauth.GoogleProfile;
import com.evolforge.core.tenancy.domain.Tenant;
import com.evolforge.core.tenancy.service.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthFacade {

    private final AuthService authService;
    private final TenantService tenantService;

    public AuthFacade(AuthService authService, TenantService tenantService) {
        this.authService = authService;
        this.tenantService = tenantService;
    }

    @Transactional
    public RegistrationResponse register(String email, String password, String displayName) {
        RegistrationResult result = authService.registerAccount(email, password, displayName);
        UserAccount user = result.user();
        Tenant tenant = tenantService.createTenantForOwner(user, displayName + " Workspace");
        return new RegistrationResponse(user.getId(), tenant.getId());
    }

    public TokenPair login(String email, String password, ClientMetadata metadata) {
        return authService.login(email, password, metadata);
    }

    public TokenPair refresh(String refreshToken, ClientMetadata metadata) {
        return authService.refresh(refreshToken, metadata);
    }

    public void resendVerificationEmail(String email) {
        authService.resendVerificationEmail(email);
    }

    public EmailVerificationResult verifyEmail(String token) {
        return authService.verifyEmail(token);
    }

    public void requestPasswordReset(String email) {
        authService.requestPasswordReset(email);
    }

    public void resetPassword(String token, String newPassword) {
        authService.resetPassword(token, newPassword);
    }

    @Transactional
    public TokenPair loginWithGoogle(GoogleProfile profile, ClientMetadata metadata) {
        GoogleAccountResult result = authService.loginWithGoogle(profile);
        if (result.newAccount()) {
            tenantService.createTenantForOwner(result.user(), profile.effectiveDisplayName() + " Workspace");
        }
        return authService.createSession(result.user(), metadata);
    }

    public record RegistrationResponse(java.util.UUID userId, java.util.UUID tenantId) {
    }
}
