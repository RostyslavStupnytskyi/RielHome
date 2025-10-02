package com.evolforge.core.igoauth;

public record GoogleProfile(String subject, String email, boolean emailVerified, String displayName) {

    public String effectiveDisplayName() {
        return displayName != null && !displayName.isBlank() ? displayName : email;
    }
}
