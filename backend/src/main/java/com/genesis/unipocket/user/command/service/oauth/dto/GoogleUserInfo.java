package com.genesis.unipocket.user.command.service.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>Google 사용자 정보 DTO</b>
 * @author bluefishez
 * @since 2026-01-29
 */
@Getter
@NoArgsConstructor
public class GoogleUserInfo implements OAuthUserInfo {

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("picture")
    private String picture;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @Override
    public String getProviderId() {
        return sub;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProfileImageUrl() {
        return picture;
    }
}