package com.genesis.unipocket.expense.command.persistence.entity.expense;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/**
 * <b>임시지출내역 엔티티</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Entity
@Getter
@Builder
@Table(name = "temporary_expense")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemporaryExpense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "temp_expense_id")
	private Long tempExpenseId;

	@Column(name = "file_id", nullable = false)
	private Long fileId;

	@Column(name = "merchant_name", nullable = false)
	private String merchantName;

	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private Category category;

	@Enumerated(EnumType.STRING)
	@Column(name = "local_country_code")
	private CurrencyCode localCountryCode;

	@Column(name = "local_currency_amount", precision = 10, scale = 2)
	private BigDecimal localCurrencyAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "base_country_code")
	private CurrencyCode baseCountryCode;

	@Column(name = "base_currency_amount", precision = 10, scale = 2)
	private BigDecimal baseCurrencyAmount;

	@Column(name = "payments_method")
	private String paymentsMethod;

	@Column(name = "memo", columnDefinition = "TEXT")
	private String memo;

	@Column(name = "occurred_at")
	private LocalDateTime occurredAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TemporaryExpenseStatus status;

	@Column(name = "card_last_four_digits", length = 4)
	private String cardLastFourDigits;

	/**
	 * 임시지출내역 상태 Enum
	 */
	public enum TemporaryExpenseStatus {
		NORMAL, // 정상
		INCOMPLETE, // 미완성
		ABNORMAL // 이상거래
	}
}
