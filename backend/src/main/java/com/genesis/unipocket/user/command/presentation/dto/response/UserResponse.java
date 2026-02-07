package com.genesis.unipocket.user.command.presentation.dto.response;

import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserRole;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserStatus;
import java.util.UUID;

public record UserResponse(
		UUID userId,
		String email,
		String name,
		String profileImgUrl,
		UserRole role,
		UserStatus status) {
	public static UserResponse from(UserEntity user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getProfileImgUrl(),
				user.getRole(),
				user.getStatus());
	}
}
