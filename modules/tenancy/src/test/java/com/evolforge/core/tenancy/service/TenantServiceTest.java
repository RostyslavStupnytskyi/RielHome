package com.evolforge.core.tenancy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.repository.UserAccountRepository;
import com.evolforge.core.infra.config.JpaConfig;
import com.evolforge.core.tenancy.domain.Membership;
import com.evolforge.core.tenancy.domain.MembershipRole;
import com.evolforge.core.tenancy.domain.Tenant;
import com.evolforge.core.tenancy.repository.MembershipRepository;
import com.evolforge.core.tenancy.repository.TenantRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
@Import({JpaConfig.class, TenantService.class})
class TenantServiceTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TenantService tenantService;

    private Tenant tenantOne;
    private Tenant tenantTwo;
    private UserAccount alice;
    private UserAccount bob;
    private UserAccount carol;

    @BeforeEach
    void setUp() {
        tenantOne = createTenant("Tenant One");
        tenantTwo = createTenant("Tenant Two");
        alice = createUser("alice@example.com", "Alice");
        bob = createUser("bob@example.com", "Bob");
        carol = createUser("carol@example.com", "Carol");
    }

    @Test
    void assignMembershipCreatesAndUpdates() {
        Membership created = tenantService.assignMembership(tenantOne, alice, MembershipRole.AGENT);
        assertThat(created.getRole()).isEqualTo(MembershipRole.AGENT);

        Membership updated = tenantService.assignMembership(tenantOne, alice, MembershipRole.ADMIN);
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getRole()).isEqualTo(MembershipRole.ADMIN);
    }

    @Test
    void updateMembershipRoleFailsWhenMissing() {
        assertThatThrownBy(() -> tenantService.updateMembershipRole(tenantOne.getId(), UUID.randomUUID(),
                MembershipRole.AGENT)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Membership not found");
    }

    @Test
    void listMembersReturnsMembersForTenantOnly() {
        tenantService.assignMembership(tenantOne, alice, MembershipRole.ADMIN);
        tenantService.assignMembership(tenantOne, bob, MembershipRole.AGENT);
        tenantService.assignMembership(tenantTwo, carol, MembershipRole.AGENT);

        List<Membership> tenantOneMembers = tenantService.listMembers(tenantOne.getId());
        assertThat(tenantOneMembers)
                .hasSize(2)
                .extracting(membership -> membership.getUser().getId())
                .containsExactlyInAnyOrder(alice.getId(), bob.getId());

        List<Membership> tenantTwoMembers = tenantService.listMembers(tenantTwo.getId());
        assertThat(tenantTwoMembers)
                .hasSize(1)
                .extracting(membership -> membership.getUser().getId())
                .containsExactly(carol.getId());
    }

    @Test
    void removeMembershipIsolatedPerTenant() {
        tenantService.assignMembership(tenantOne, alice, MembershipRole.ADMIN);
        tenantService.assignMembership(tenantTwo, alice, MembershipRole.AGENT);

        boolean removed = tenantService.removeMembership(tenantOne.getId(), alice.getId());
        assertThat(removed).isTrue();
        assertThat(membershipRepository.findByUserIdAndTenantId(alice.getId(), tenantOne.getId())).isEmpty();
        assertThat(membershipRepository.findByUserIdAndTenantId(alice.getId(), tenantTwo.getId())).isPresent();
    }

    private Tenant createTenant(String name) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        return tenantRepository.saveAndFlush(tenant);
    }

    private UserAccount createUser(String email, String displayName) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setEmailVerified(true);
        user.setDisabled(false);
        return userAccountRepository.saveAndFlush(user);
    }
}
