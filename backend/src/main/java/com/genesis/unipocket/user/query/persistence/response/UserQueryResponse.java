package com.genesis.unipocket.user.query.persistence.response;

import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserRole;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserStatus;
import java.util.UUID;

public record UserQueryResponse(
		UUID userId,
		String email,
		String name,
		String profileImgUrl,
		UserRole role,
		UserStatus status) {
	public static UserQueryResponse from(UserEntity user) {
		return new UserQueryResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getProfileImgUrl(),
				user.getRole(),
				user.getStatus());
	}
}
