package com.genesis.unipocket.user.command.facade.port.dto;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import java.util.UUID;

public record SocialAuthCreate(
		UUID userId, ProviderType providerType, String providerId, String email) {
	public static SocialAuthCreate of(
			UUID userId, ProviderType providerType, String providerId, String email) {
		return new SocialAuthCreate(userId, providerType, providerId, email);
	}
}
