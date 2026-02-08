package com.genesis.unipocket.user.dto.request;

import com.genesis.unipocket.user.persistence.entity.enums.CardCompany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserCardRequest(
		String nickName,
		@NotBlank(message = "카드 번호는 필수입니다.") @Pattern(regexp = "\\d{4}", message = "카드 번호는 4자리 숫자여야 합니다.") String cardNumber,
		@jakarta.validation.constraints.NotNull(message = "카드사는 필수입니다.") CardCompany cardCompany) {}
