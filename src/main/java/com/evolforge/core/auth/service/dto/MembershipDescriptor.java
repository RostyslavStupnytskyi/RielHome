package com.evolforge.core.auth.service.dto;

import java.util.UUID;

public record MembershipDescriptor(UUID tenantId, String role) {
}
