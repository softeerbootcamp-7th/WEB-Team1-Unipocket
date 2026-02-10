package com.genesis.unipocket.user.command.application.command;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;

public record RegisterUserCommand(
		ProviderType providerType,
		String providerId,
		String email,
		String name,
		String profileImageUrl) {
	public static RegisterUserCommand of(
			ProviderType providerType,
			String providerId,
			String email,
			String name,
			String profileImageUrl) {
		return new RegisterUserCommand(providerType, providerId, email, name, profileImageUrl);
	}
}
