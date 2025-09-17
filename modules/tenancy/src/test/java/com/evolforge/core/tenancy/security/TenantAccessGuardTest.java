package com.evolforge.core.tenancy.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.evolforge.core.tenancy.context.TenantContext;
import com.evolforge.core.tenancy.context.TenantContextHolder;
import com.evolforge.core.tenancy.domain.MembershipRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TenantAccessGuardTest {

    private final AuthorizationContext authorizationContext = null;

    @Test
    void grantsAccessWhenRoleMatches() {
        TenantAccessGuard guard = new TenantAccessGuard(MembershipRole.ADMIN, MembershipRole.OWNER);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "token");
        authentication.setAuthenticated(true);

        Mono<AuthorizationDecision> decisionMono = guard.check(Mono.just(authentication), authorizationContext)
                .contextWrite(ctx -> TenantContextHolder.put(ctx,
                        new TenantContext(UUID.randomUUID(), MembershipRole.ADMIN)));

        StepVerifier.create(decisionMono)
                .assertNext(decision -> assertThat(decision.isGranted()).isTrue())
                .verifyComplete();
    }

    @Test
    void deniesAccessWhenRoleNotAllowed() {
        TenantAccessGuard guard = new TenantAccessGuard(MembershipRole.OWNER);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "token");
        authentication.setAuthenticated(true);

        Mono<AuthorizationDecision> decisionMono = guard.check(Mono.just(authentication), authorizationContext)
                .contextWrite(ctx -> TenantContextHolder.put(ctx,
                        new TenantContext(UUID.randomUUID(), MembershipRole.AGENT)));

        StepVerifier.create(decisionMono)
                .assertNext(decision -> assertThat(decision.isGranted()).isFalse())
                .verifyComplete();
    }

    @Test
    void deniesAccessWhenContextMissing() {
        TenantAccessGuard guard = new TenantAccessGuard(MembershipRole.AGENT);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "token");
        authentication.setAuthenticated(true);

        Mono<AuthorizationDecision> decisionMono = guard.check(Mono.just(authentication), authorizationContext);

        StepVerifier.create(decisionMono)
                .assertNext(decision -> assertThat(decision.isGranted()).isFalse())
                .verifyComplete();
    }
}
