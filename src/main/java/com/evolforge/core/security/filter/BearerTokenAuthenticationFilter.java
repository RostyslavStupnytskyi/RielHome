package com.evolforge.core.security.filter;

import com.evolforge.core.security.JwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final RequestMatcher requiresAuthentication;

    public BearerTokenAuthenticationFilter(AuthenticationManager authenticationManager,
            RequestMatcher requiresAuthentication) {
        this.authenticationManager = authenticationManager;
        this.requiresAuthentication = requiresAuthentication;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requiresAuthentication.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = resolveToken(request);
            if (StringUtils.hasText(token)) {
                JwtAuthenticationToken authenticationRequest = JwtAuthenticationToken.unauthenticated(token);
                try {
                    Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authenticationResult);
                    SecurityContextHolder.setContext(context);
                } catch (AuthenticationException ex) {
                    SecurityContextHolder.clearContext();
                    throw ex;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return StringUtils.hasText(token) ? token : null;
    }
}
