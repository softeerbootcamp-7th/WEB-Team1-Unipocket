package com.genesis.unipocket.user.command.presentation.request;

import com.genesis.unipocket.user.common.enums.CardCompany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserCardCreateRequest(
		String nickName,
		@NotBlank(message = "카드 번호는 필수입니다.") @Pattern(regexp = "\\d{4}", message = "카드 번호는 4자리 숫자여야 합니다.") String cardNumber,
		@NotNull(message = "카드사는 필수입니다.") CardCompany cardCompany) {}
