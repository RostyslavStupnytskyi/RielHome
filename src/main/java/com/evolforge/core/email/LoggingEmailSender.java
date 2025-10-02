package com.evolforge.core.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmailSender.class);

    private final EmailProperties properties;

    public LoggingEmailSender(EmailProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendVerificationEmail(String to, String displayName, String verificationLink) {
        LOGGER.info("Sending verification email to {} from {} with link {}", to, properties.getFrom(),
                verificationLink);
    }

    @Override
    public void sendPasswordResetEmail(String to, String displayName, String resetLink) {
        LOGGER.info("Sending password reset email to {} from {} with link {}", to, properties.getFrom(), resetLink);
    }
}
