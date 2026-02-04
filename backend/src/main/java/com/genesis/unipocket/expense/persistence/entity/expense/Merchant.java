package com.genesis.unipocket.expense.persistence.entity.expense;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Merchant {

	@Column(nullable = false, length = 40)
	private String merchantName; // 거래내역 명(원본)

	@Column(length = 40)
	@Setter
	private String displayMerchantName; // 거래내역 명(유저 수정)

	public static Merchant of(String merchantName) {
		if (merchantName == null || merchantName.trim().isEmpty()) {
			throw new IllegalArgumentException("merchantName must not be blank");
		}

		String normalized = merchantName.trim();

		Merchant merchant = new Merchant();
		merchant.merchantName = normalized;
		merchant.displayMerchantName = normalized;
		return merchant;
	}
}
