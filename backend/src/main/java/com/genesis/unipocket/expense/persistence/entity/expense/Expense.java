package com.genesis.unipocket.expense.persistence.entity.expense;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.persistence.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.global.base.BaseEntity;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/**
 * <b>지출내역 엔티티</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Entity
@Getter
@Builder
@Table(name = "expenses")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long expenseId;

	private Long accountBookId;

	@Column(length = 64)
	private String approvalNumber;

	private Category category;

	private String paymentMethod;

	private Long travelId;

	@Column(length = 40)
	private String memo;

	@Column(nullable = false)
	private LocalDateTime occurredAt;

	@Embedded private Merchant merchant;

	@Embedded private ExpenseSourceInfo expenseSourceInfo;

	@Embedded private OriginExchangeInfo originExchangeInfo;

	@Embedded private CachedExchangeInfo cachedExchangeInfo;

	public static Expense manual(ExpenseManualCreateArgs params) {
		if (params == null) {
			throw new IllegalArgumentException("params must not be null");
		}

		return Expense.builder()
				.accountBookId(params.accountBookId())
				.merchant(Merchant.of(params.merchantName()))
				.occurredAt(params.occurredAt())
				.memo(params.memo())
				.paymentMethod(params.paymentMethod())
				.category(params.category())
				.expenseSourceInfo(ExpenseSourceInfo.ofManual())
				.originExchangeInfo(
						OriginExchangeInfo.of(
								params.localCurrency(),
								params.standardCurrency(),
								params.localAmount(),
								params.standardAmount()))
				.build();
	}

	public String getMerchantName() {
		return merchant.getDisplayMerchantName() != null
				? merchant.getDisplayMerchantName()
				: merchant.getMerchantName();
	}

	public CurrencyCode getLocalCurrency() {
		return originExchangeInfo != null ? originExchangeInfo.getLocalCurrencyCode() : null;
	}

	public BigDecimal getLocalAmount() {
		return originExchangeInfo != null ? originExchangeInfo.getLocalCurrencyAmount() : null;
	}

	public CurrencyCode getStandardCurrency() {
		return cachedExchangeInfo != null
				? cachedExchangeInfo.getStandardCurrencyCode()
				: originExchangeInfo.getBillingCurrencyCode();
	}

	public BigDecimal getStandardAmount() {
		return cachedExchangeInfo != null
				? cachedExchangeInfo.getStandardCurrencyAmount()
				: originExchangeInfo.getBillingCurrencyAmount();
	}
}
