package com.genesis.unipocket.user.query.persistence.response;

import com.genesis.unipocket.user.common.enums.UserRole;
import com.genesis.unipocket.user.common.enums.UserStatus;
import java.util.UUID;

public record UserQueryResponse(
		UUID userId,
		String email,
		String name,
		String profileImgUrl,
		UserRole role,
		UserStatus status,
		boolean needsOnboarding) {

	public UserQueryResponse(
			UUID userId,
			String email,
			String name,
			String profileImgUrl,
			UserRole role,
			UserStatus status) {
		this(userId, email, name, profileImgUrl, role, status, false);
	}

	public UserQueryResponse withNeedsOnboarding(boolean needsOnboarding) {
		return new UserQueryResponse(
				userId, email, name, profileImgUrl, role, status, needsOnboarding);
	}
}
