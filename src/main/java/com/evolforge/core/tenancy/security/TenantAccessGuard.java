package com.evolforge.core.tenancy.security;

import com.evolforge.core.tenancy.context.TenantContextHolder;
import com.evolforge.core.tenancy.domain.MembershipRole;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Authorization manager that checks whether the current caller has one of the required roles within the
 * resolved tenant context.
 */
public class TenantAccessGuard implements AuthorizationManager<RequestAuthorizationContext> {

    private final Set<MembershipRole> allowedRoles;

    public TenantAccessGuard(MembershipRole... allowedRoles) {
        if (allowedRoles == null || allowedRoles.length == 0) {
            throw new IllegalArgumentException("At least one membership role must be provided");
        }
        EnumSet<MembershipRole> roleSet = EnumSet.noneOf(MembershipRole.class);
        for (MembershipRole role : allowedRoles) {
            if (role != null) {
                roleSet.add(role);
            }
        }
        if (roleSet.isEmpty()) {
            throw new IllegalArgumentException("At least one membership role must be provided");
        }
        this.allowedRoles = Set.copyOf(roleSet);
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        boolean allowed = TenantContextHolder.currentContext()
                .map(tenantContext -> allowedRoles.contains(tenantContext.role()))
                .orElse(false);
        return new AuthorizationDecision(allowed);
    }
}
