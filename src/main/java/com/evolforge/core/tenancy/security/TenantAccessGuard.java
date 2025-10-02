package com.evolforge.core.tenancy.security;

import com.evolforge.core.tenancy.context.TenantContextHolder;
import com.evolforge.core.tenancy.domain.MembershipRole;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * Reactive authorization manager that checks whether the current caller has one of the required roles
 * within the resolved tenant context.
 */
public class TenantAccessGuard implements ReactiveAuthorizationManager<AuthorizationContext> {

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
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> TenantContextHolder.currentContext()
                        .map(tenantContext -> new AuthorizationDecision(allowedRoles.contains(tenantContext.role())))
                        .defaultIfEmpty(new AuthorizationDecision(false)))
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
