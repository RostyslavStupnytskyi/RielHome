package com.evolforge.core.email;

public interface EmailSender {

    void sendVerificationEmail(String to, String displayName, String verificationLink);

    void sendPasswordResetEmail(String to, String displayName, String resetLink);
}
