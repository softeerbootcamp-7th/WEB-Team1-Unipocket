package com.genesis.unipocket.tempexpense.command.persistence.entity;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
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

	@Column(name = "temp_expense_meta_id", nullable = false)
	private Long tempExpenseMetaId;

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

	@Column(name = "exchange_rate", precision = 10, scale = 4)
	private BigDecimal exchangeRate;

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

	@Column(name = "approval_number")
	private String approvalNumber;
}
