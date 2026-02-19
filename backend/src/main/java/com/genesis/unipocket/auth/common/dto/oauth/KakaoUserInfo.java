package com.genesis.unipocket.auth.common.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>Kakao 사용자 정보 DTO</b>
 *
 * @author 김동균
 * @since 2026-01-29
 */
@Getter
@NoArgsConstructor
public class KakaoUserInfo implements OAuthUserInfo {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;

	@JsonProperty("properties")
	private Properties properties;

	@Getter
	@NoArgsConstructor
	public static class KakaoAccount {
		@JsonProperty("email")
		private String email;

		@JsonProperty("profile")
		private Profile profile;
	}

	@Getter
	@NoArgsConstructor
	public static class Profile {
		@JsonProperty("nickname")
		private String nickname;

		@JsonProperty("profile_image_url")
		private String profileImageUrl;
	}

	@Getter
	@NoArgsConstructor
	public static class Properties {
		@JsonProperty("nickname")
		private String nickname;

		@JsonProperty("profile_image")
		private String profileImage;
	}

	@Override
	public String getProviderId() {
		return String.valueOf(id);
	}

	@Override
	public String getEmail() {
		return kakaoAccount != null ? kakaoAccount.getEmail() : null;
	}

	@Override
	public String getName() {
		if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
			return kakaoAccount.getProfile().getNickname();
		}
		return properties != null ? properties.getNickname() : null;
	}

	@Override
	public String getProfileImageUrl() {
		if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
			return kakaoAccount.getProfile().getProfileImageUrl();
		}
		return properties != null ? properties.getProfileImage() : null;
	}
}
