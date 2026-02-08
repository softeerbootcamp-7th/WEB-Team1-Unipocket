package com.genesis.unipocket.expense.persistence.entity.expense;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.global.common.entity.BaseEntity;
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
@Table(
		name = "expenses",
		indexes = {
			// 필수: 가계부별 조회 (FK)
			@Index(name = "idx_expenses_account_book_id", columnList = "accountBookId"),

			// 필수: 가계부 + 날짜 복합 인덱스 (목록 조회 최적화)
			@Index(
					name = "idx_expenses_account_book_occurred",
					columnList = "accountBookId, occurredAt DESC"),

			// 선택: 카테고리 필터링
			@Index(name = "idx_expenses_category", columnList = "category"),

			// 선택: 여행별 조회
			@Index(name = "idx_expenses_travel_id", columnList = "travelId")
		})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long expenseId;

	@Column(nullable = false)
	private Long accountBookId;

	@Column(length = 64)
	private String approvalNumber;

	@Column(length = 4)
	private String cardNumber;

	private Category category;

	private String paymentMethod;

	private Long travelId;

	@Column(length = 40)
	private String memo;

	@Column(nullable = false)
	private LocalDateTime occurredAt;

	@Embedded private Merchant merchant;

	@Embedded private ExpenseSourceInfo expenseSourceInfo;

	@Embedded private ExchangeInfo exchangeInfo;

	public static ExpenseEntity manual(ExpenseManualCreateArgs params) {
		if (params == null) {
			throw new IllegalArgumentException("params must not be null");
		}

		return ExpenseEntity.builder()
				.accountBookId(params.accountBookId())
				.merchant(Merchant.of(params.merchantName()))
				.occurredAt(params.occurredAt())
				.memo(params.memo())
				.paymentMethod(params.paymentMethod())
				.category(params.category())
				.expenseSourceInfo(ExpenseSourceInfo.ofManual())
				.exchangeInfo(
						ExchangeInfo.of(
								params.localCurrencyCode(),
								params.baseCurrencyCode(),
								params.localCurrencyAmount(),
								params.baseCurrencyAmount()))
				.build();
	}

	public String getMerchantName() {
		return merchant.getDisplayMerchantName() != null
				? merchant.getDisplayMerchantName()
				: merchant.getMerchantName();
	}

	public CurrencyCode getLocalCurrency() {
		return exchangeInfo != null ? exchangeInfo.getLocalCurrencyCode() : null;
	}

	public BigDecimal getLocalAmount() {
		return exchangeInfo != null ? exchangeInfo.getLocalCurrencyAmount() : null;
	}

	public CurrencyCode getStandardCurrency() {
		return exchangeInfo != null ? exchangeInfo.getBaseCurrencyCode() : null;
	}

	public BigDecimal getStandardAmount() {
		return exchangeInfo != null ? exchangeInfo.getBaseCurrencyAmount() : null;
	}

	// Update methods
	public void updateMerchantName(String merchantName) {
		if (merchantName == null || merchantName.trim().isEmpty()) {
			throw new IllegalArgumentException("merchantName must not be blank");
		}
		this.merchant.setDisplayMerchantName(merchantName.trim());
	}

	public void updateCategory(Category category) {
		this.category = category;
	}

	public void updatePaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public void updateMemo(String memo) {
		this.memo = memo;
	}

	public void updateOccurredAt(LocalDateTime occurredAt) {
		if (occurredAt == null) {
			throw new IllegalArgumentException("occurredAt must not be null");
		}
		this.occurredAt = occurredAt;
	}

	public void updateTravelId(Long travelId) {
		this.travelId = travelId;
	}

	public void updateExchangeInfo(
			CurrencyCode localCurrencyCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount) {
		// ExchangeInfo는 immutable이므로 전체 교체
		this.exchangeInfo =
				ExchangeInfo.of(
						localCurrencyCode,
						baseCurrencyCode,
						localCurrencyAmount,
						baseCurrencyAmount);
	}
}
