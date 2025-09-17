package com.evolforge.core.security;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String token;

    private JwtAuthenticationToken(Object principal, String token,
            Collection<? extends GrantedAuthority> authorities, boolean authenticated) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        super.setAuthenticated(authenticated);
    }

    public static JwtAuthenticationToken unauthenticated(String token) {
        return new JwtAuthenticationToken(null, token, Collections.emptyList(), false);
    }

    public static JwtAuthenticationToken authenticated(JwtPrincipal principal, String token,
            Collection<? extends GrantedAuthority> authorities) {
        return new JwtAuthenticationToken(principal, token, authorities, true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted via setter");
        }
        super.setAuthenticated(false);
    }
}
