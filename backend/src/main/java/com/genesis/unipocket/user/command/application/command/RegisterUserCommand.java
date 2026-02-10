package com.genesis.unipocket.user.command.application.command;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.common.OAuthUserInfo;

public record RegisterUserCommand(OAuthUserInfo userInfo, ProviderType providerType) {
	public static RegisterUserCommand of(OAuthUserInfo userInfo, ProviderType providerType) {
		return new RegisterUserCommand(userInfo, providerType);
	}
}
