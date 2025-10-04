package com.evolforge.core.security.filter;

import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.evolforge.core.tenancy.context.TenantAwarePrincipal;
import com.evolforge.core.tenancy.context.TenantContext;
import com.evolforge.core.tenancy.context.TenantContextHolder;
import com.evolforge.core.tenancy.domain.MembershipRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional<TenantContext> tenantContext = resolveTenantContext(request);
        tenantContext.ifPresent(TenantContextHolder::set);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private Optional<TenantContext> resolveTenantContext(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof TenantAwarePrincipal tenantAware)) {
            return Optional.empty();
        }
        List<MembershipDescriptor> memberships = tenantAware.memberships();
        if (memberships == null || memberships.isEmpty()) {
            return Optional.empty();
        }
        UUID requestedTenantId = extractTenantId(request.getHeader(TENANT_HEADER));
        if (requestedTenantId != null) {
            return membershipForTenant(memberships, requestedTenantId);
        }
        if (memberships.size() == 1) {
            return toContext(memberships.getFirst());
        }
        return Optional.empty();
    }

    private Optional<TenantContext> membershipForTenant(List<MembershipDescriptor> memberships, UUID tenantId) {
        return memberships.stream()
                .filter(descriptor -> tenantId.equals(descriptor.tenantId()))
                .findFirst()
                .flatMap(this::toContext);
    }

    private Optional<TenantContext> toContext(MembershipDescriptor descriptor) {
        if (descriptor == null || descriptor.tenantId() == null || !StringUtils.hasText(descriptor.role())) {
            return Optional.empty();
        }
        try {
            MembershipRole role = MembershipRole.valueOf(descriptor.role().trim().toUpperCase(Locale.ROOT));
            return Optional.of(new TenantContext(descriptor.tenantId(), role));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
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
}
