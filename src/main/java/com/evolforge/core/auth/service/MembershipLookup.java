package com.evolforge.core.auth.service;

import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import java.util.List;
import java.util.UUID;

public interface MembershipLookup {

    List<MembershipDescriptor> membershipsForUser(UUID userId);
}
