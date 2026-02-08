package com.genesis.unipocket.user.command.service.dto;

import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserRole;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserStatus;
import java.util.UUID;

/**
 * Service 계층용 User DTO
 */
public record UserDto(
		UUID userId,
		String email,
		String name,
		String profileImgUrl,
		UserRole role,
		UserStatus status) {

	public static UserDto from(UserEntity user) {
		return new UserDto(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getProfileImgUrl(),
				user.getRole(),
				user.getStatus());
	}
}
