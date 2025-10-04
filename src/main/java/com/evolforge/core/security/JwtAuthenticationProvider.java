package com.evolforge.core.security;

import com.evolforge.core.auth.exception.AuthException;
import com.evolforge.core.auth.service.JwtService;
import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;

    public JwtAuthenticationProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            return null;
        }
        String token = (String) jwtToken.getCredentials();
        try {
            JwtService.AccessTokenDetails details = jwtService.parse(token);
            return toAuthentication(details, token);
        } catch (AuthException ex) {
            throw new BadCredentialsException("Invalid access token", ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
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
