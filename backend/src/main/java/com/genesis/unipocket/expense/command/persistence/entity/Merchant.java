package com.genesis.unipocket.expense.command.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Merchant {

	@Column(nullable = false, length = 40)
	@Setter
	private String displayMerchantName; // 거래내역 표시명

	public static Merchant of(String merchantName) {
		if (merchantName == null || merchantName.trim().isEmpty()) {
			throw new IllegalArgumentException("merchantName must not be blank");
		}

		String normalized = merchantName.trim();

		Merchant merchant = new Merchant();
		merchant.displayMerchantName = normalized;
		return merchant;
	}

	// Backward compatibility: 도메인 내부에서 기존 getter 호출 시 표시명을 반환
	public String getMerchantName() {
		return displayMerchantName;
	}
}
