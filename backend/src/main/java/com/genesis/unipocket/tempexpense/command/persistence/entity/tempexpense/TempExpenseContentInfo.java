package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import com.genesis.unipocket.global.common.enums.Category;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TempExpenseContentInfo {

	private String merchantName;

	@Enumerated(EnumType.STRING)
	private Category category;

	private String memo;
	private LocalDateTime occurredAt;

	private TempExpenseContentInfo(
			String merchantName, Category category, String memo, LocalDateTime occurredAt) {
		this.merchantName = merchantName;
		this.category = category;
		this.memo = memo;
		this.occurredAt = occurredAt;
	}

	public static TempExpenseContentInfo of(
			String merchantName, Category category, String memo, LocalDateTime occurredAt) {
		return new TempExpenseContentInfo(trimToNull(merchantName), category, memo, occurredAt);
	}

	public TempExpenseContentInfo merge(
			String merchantName, Category category, String memo, LocalDateTime occurredAt) {
		return new TempExpenseContentInfo(
				merchantName != null ? trimToNull(merchantName) : this.merchantName,
				category != null ? category : this.category,
				memo != null ? memo : this.memo,
				occurredAt != null ? occurredAt : this.occurredAt);
	}

	public boolean hasRequired() {
		return merchantName != null && category != null && occurredAt != null;
	}

	private static String trimToNull(String v) {
		if (v == null) return null;
		String t = v.trim();
		return t.isEmpty() ? null : t;
	}
}
