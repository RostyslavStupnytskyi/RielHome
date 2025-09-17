package com.evolforge.core.tenancy.context;

import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.evolforge.core.tenancy.domain.MembershipRole;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Resolves the tenant for the current request from either the JWT memberships or the X-Tenant-Id header.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class TenantResolver implements WebFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return resolveTenantContext(exchange)
                .flatMap(tenantContext -> chain.filter(exchange)
                        .contextWrite(ctx -> TenantContextHolder.put(ctx, tenantContext)))
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<TenantContext> resolveTenantContext(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .filter(Authentication::isAuthenticated)
                .flatMap(authentication -> {
                    Object principal = authentication.getPrincipal();
                    if (!(principal instanceof TenantAwarePrincipal tenantAware)) {
                        return Mono.empty();
                    }
                    List<MembershipDescriptor> memberships = tenantAware.memberships();
                    if (memberships == null || memberships.isEmpty()) {
                        return Mono.empty();
                    }
                    UUID requestedTenantId = extractTenantId(exchange.getRequest().getHeaders().getFirst(TENANT_HEADER));
                    if (requestedTenantId != null) {
                        return membershipForTenant(memberships, requestedTenantId);
                    }
                    if (memberships.size() == 1) {
                        return toContext(memberships.getFirst());
                    }
                    return Mono.empty();
                });
    }

    private UUID extractTenantId(String rawTenantId) {
        if (!StringUtils.hasText(rawTenantId)) {
            return null;
        }
        try {
            return UUID.fromString(rawTenantId.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Mono<TenantContext> membershipForTenant(List<MembershipDescriptor> memberships, UUID tenantId) {
        return memberships.stream()
                .filter(descriptor -> tenantId.equals(descriptor.tenantId()))
                .findFirst()
                .map(this::toContext)
                .orElse(Mono.empty());
    }

    private Mono<TenantContext> toContext(MembershipDescriptor descriptor) {
        if (descriptor == null || descriptor.tenantId() == null || !StringUtils.hasText(descriptor.role())) {
            return Mono.empty();
        }
        try {
            MembershipRole role = MembershipRole.valueOf(descriptor.role().trim().toUpperCase(Locale.ROOT));
            return Mono.just(new TenantContext(descriptor.tenantId(), role));
        } catch (IllegalArgumentException ex) {
            return Mono.empty();
        }
    }
}
