package com.genesis.unipocket.user.command.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>로그인 요청 DTO</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Getter
@NoArgsConstructor
public class LoginRequest {

    private String email;

    private String password;
}