package com.evolforge.core.tenancy.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.evolforge.core.tenancy.domain.MembershipRole;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TenantResolverTest {

    private final TenantResolver resolver = new TenantResolver();

    @Test
    void resolvesTenantFromHeaderWhenMembershipExists() {
        UUID tenantId = UUID.randomUUID();
        MembershipDescriptor descriptor = new MembershipDescriptor(tenantId, MembershipRole.ADMIN.name());
        TestPrincipal principal = new TestPrincipal("user", List.of(descriptor));
        Authentication authentication = authenticated(principal);

        AtomicReference<TenantContext> contextRef = new AtomicReference<>();
        ServerWebExchange exchange = withRequest(MockServerHttpRequest.get("/api/test")
                .header(TenantResolver.TENANT_HEADER, tenantId.toString())
                .build());
        WebFilterChain chain = serverWebExchange -> TenantContextHolder.currentContext()
                .doOnNext(contextRef::set)
                .then();

        Mono<Void> result = resolver.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        StepVerifier.create(result).verifyComplete();
        assertThat(contextRef.get()).isNotNull();
        assertThat(contextRef.get().tenantId()).isEqualTo(tenantId);
        assertThat(contextRef.get().role()).isEqualTo(MembershipRole.ADMIN);
    }

    @Test
    void resolvesTenantWhenSingleMembershipAndNoHeader() {
        UUID tenantId = UUID.randomUUID();
        MembershipDescriptor descriptor = new MembershipDescriptor(tenantId, MembershipRole.AGENT.name());
        TestPrincipal principal = new TestPrincipal("user", List.of(descriptor));
        Authentication authentication = authenticated(principal);

        AtomicReference<TenantContext> contextRef = new AtomicReference<>();
        ServerWebExchange exchange = withRequest(MockServerHttpRequest.get("/api/test").build());
        WebFilterChain chain = serverWebExchange -> TenantContextHolder.currentContext()
                .doOnNext(contextRef::set)
                .then();

        Mono<Void> result = resolver.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        StepVerifier.create(result).verifyComplete();
        assertThat(contextRef.get()).isNotNull();
        assertThat(contextRef.get().tenantId()).isEqualTo(tenantId);
        assertThat(contextRef.get().role()).isEqualTo(MembershipRole.AGENT);
    }

    @Test
    void doesNotResolveWhenMembershipMissing() {
        UUID tenantId = UUID.randomUUID();
        MembershipDescriptor descriptor = new MembershipDescriptor(UUID.randomUUID(), MembershipRole.ADMIN.name());
        TestPrincipal principal = new TestPrincipal("user", List.of(descriptor));
        Authentication authentication = authenticated(principal);

        AtomicReference<TenantContext> contextRef = new AtomicReference<>();
        ServerWebExchange exchange = withRequest(MockServerHttpRequest.get("/api/test")
                .header(TenantResolver.TENANT_HEADER, tenantId.toString())
                .build());
        WebFilterChain chain = serverWebExchange -> TenantContextHolder.currentContext()
                .doOnNext(contextRef::set)
                .then();

        Mono<Void> result = resolver.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        StepVerifier.create(result).verifyComplete();
        assertThat(contextRef.get()).isNull();
    }

    private Authentication authenticated(Principal principal) {
        return new UsernamePasswordAuthenticationToken(principal, "token",
                AuthorityUtils.createAuthorityList("ROLE_USER"));
    }

    private ServerWebExchange withRequest(MockServerHttpRequest request) {
        return MockServerWebExchange.from(request);
    }

    private record TestPrincipal(String name, List<MembershipDescriptor> memberships)
            implements Principal, TenantAwarePrincipal {

        @Override
        public String getName() {
            return name;
        }
    }
}
