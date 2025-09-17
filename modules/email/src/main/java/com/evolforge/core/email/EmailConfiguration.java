package com.evolforge.core.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    public EmailSender emailSender(EmailProperties properties) {
        return new LoggingEmailSender(properties);
    }
}
