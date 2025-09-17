package com.evolforge.core.auth.service.dto;

import com.evolforge.core.auth.domain.UserAccount;

public record GoogleAccountResult(UserAccount user, boolean newAccount) {
}
