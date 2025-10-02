package com.evolforge.core.security;

import com.evolforge.core.auth.exception.AuthException;
import com.evolforge.core.auth.service.JwtService;
import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public JwtReactiveAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            return Mono.empty();
        }
        String token = (String) jwtToken.getCredentials();
        return Mono.fromCallable(() -> jwtService.parse(token))
                .map(details -> toAuthentication(details, token))
                .onErrorMap(AuthException.class, ex -> new BadCredentialsException("Invalid access token", ex));
    }

    private Authentication toAuthentication(JwtService.AccessTokenDetails details, String tokenValue) {
        List<GrantedAuthority> authorities = details.memberships().stream()
                .map(MembershipDescriptor::role)
                .filter(role -> role != null && !role.isBlank())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toUnmodifiableList());
        JwtPrincipal principal = new JwtPrincipal(details.userId(), details.email(), details.displayName(),
                details.memberships(), details.tokenId());
        return JwtAuthenticationToken.authenticated(principal, tokenValue, authorities);
    }
}
