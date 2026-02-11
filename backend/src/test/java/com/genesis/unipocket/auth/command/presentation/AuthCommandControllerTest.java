package com.genesis.unipocket.auth.command.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.auth.command.application.AuthService;
import com.genesis.unipocket.auth.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.auth.command.facade.UserLoginFacade;
import com.genesis.unipocket.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthCommandController.class)
class AuthCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CookieUtil cookieUtil;

    @MockBean
    private OAuthAuthorizeFacade authorizeFacade;

    @MockBean
    private UserLoginFacade loginFacade;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private com.genesis.unipocket.auth.command.application.JwtProvider jwtProvider;

    @MockBean
    private com.genesis.unipocket.auth.command.application.TokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_Success() throws Exception {
        // given
        String refreshToken = "valid_refresh_token";
        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";
        AuthService.TokenPair tokenPair = new AuthService.TokenPair(newAccessToken, newRefreshToken);

        given(authService.reissue(refreshToken)).willReturn(tokenPair);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk());

        verify(authService).reissue(refreshToken);
        verify(cookieUtil).addCookie(any(HttpServletResponse.class), eq("access_token"), eq(newAccessToken), anyInt());
        verify(cookieUtil).addCookie(any(HttpServletResponse.class), eq("refresh_token"), eq(newRefreshToken),
                anyInt());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // given
        String accessToken = "valid_access_token";
        String refreshToken = "valid_refresh_token";

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("access_token", accessToken))
                .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk());

        verify(authService).logout(accessToken, refreshToken);
        verify(cookieUtil).deleteCookie(any(HttpServletResponse.class), eq("access_token"));
        verify(cookieUtil).deleteCookie(any(HttpServletResponse.class), eq("refresh_token"));
    }
}
