package com.genesis.unipocket.expense.entity.expense;

import com.genesis.unipocket.expense.common.enums.ExpenseSource;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseSourceInfo {

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 32)
	private ExpenseSource expenseSource;

	@Column(name = "file_link", length = 1000)
	private String fileLink;

	private ExpenseSourceInfo(ExpenseSource expenseSource, String fileLink) {
		this.expenseSource = java.util.Objects.requireNonNull(expenseSource, "sourceType");
		this.fileLink = fileLink;
	}

	public static ExpenseSourceInfo ofManual() {
		return new ExpenseSourceInfo(ExpenseSource.MANUAL, null);
	}
}
