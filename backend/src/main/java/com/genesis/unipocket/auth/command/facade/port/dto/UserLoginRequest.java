package com.genesis.unipocket.auth.command.facade.port.dto;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;

public record UserLoginRequest(
		ProviderType providerType,
		String providerId,
		String email,
		String name,
		String profileImageUrl) {
	public static UserLoginRequest of(
			ProviderType providerType,
			String providerId,
			String email,
			String name,
			String profileImageUrl) {
		return new UserLoginRequest(providerType, providerId, email, name, profileImageUrl);
	}
}
