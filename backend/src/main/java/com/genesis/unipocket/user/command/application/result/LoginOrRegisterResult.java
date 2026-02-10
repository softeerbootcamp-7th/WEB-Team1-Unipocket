package com.genesis.unipocket.user.command.application.result;

public record LoginOrRegisterResult(String accessToken, String refreshToken, Long expiresIn) {
	public static LoginOrRegisterResult of(
			String accessToken, String refreshToken, Long expiresIn) {
		return new LoginOrRegisterResult(accessToken, refreshToken, expiresIn);
	}
}
