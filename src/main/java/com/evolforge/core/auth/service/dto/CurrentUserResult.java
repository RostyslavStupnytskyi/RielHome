package com.evolforge.core.auth.service.dto;

import com.evolforge.core.auth.domain.UserAccount;
import java.util.List;

public record CurrentUserResult(UserAccount user, List<MembershipDescriptor> memberships) {
}
