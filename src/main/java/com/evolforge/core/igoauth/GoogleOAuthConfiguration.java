package com.evolforge.core.igoauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class GoogleOAuthConfiguration {

    @Bean
    public WebClient googleOAuthWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}
