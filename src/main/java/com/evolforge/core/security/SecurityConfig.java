package com.evolforge.core.security;

import com.evolforge.core.security.filter.AuthRateLimitingFilter;
import com.evolforge.core.security.filter.BearerTokenAuthenticationFilter;
import com.evolforge.core.security.filter.TenantContextFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_API_PATHS = {
            "/api/auth/**",
            "/",
            "/index.html",
            "/favicon.ico",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/v3/api-docs/**"
    };

    private static final String[] PUBLIC_STATIC_RESOURCES = {
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**",
            "/assets/**"
    };

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(com.evolforge.core.auth.service.JwtService jwtService) {
        return new JwtAuthenticationProvider(jwtService);
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new ProviderManager(jwtAuthenticationProvider);
    }

    @Bean
    public BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        RequestMatcher requiresAuth = new AntPathRequestMatcher("/api/**");
        return new BearerTokenAuthenticationFilter(authenticationManager, requiresAuth);
    }

    @Bean
    @Order(0)
    public SecurityFilterChain actuatorSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http)
            throws Exception {
        return http
                .securityMatcher(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
            AuthenticationManager authenticationManager,
            JwtAuthenticationProvider jwtAuthenticationProvider,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter,
            AuthRateLimitingFilter authRateLimitingFilter,
            TenantContextFilter tenantContextFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(authenticationManager)
                .authenticationProvider(jwtAuthenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                        .requestMatchers(PUBLIC_API_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_STATIC_RESOURCES).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authRateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }
}
