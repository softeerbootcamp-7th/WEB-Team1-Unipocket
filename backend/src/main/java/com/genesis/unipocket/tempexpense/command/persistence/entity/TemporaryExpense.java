package com.genesis.unipocket.tempexpense.command.persistence.entity;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseAmountInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseContentInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpensePatch;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpensePaymentInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseStatusPolicy;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Table(name = "temporary_expense")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemporaryExpense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "temp_expense_id")
	private Long tempExpenseId;

	@Column(name = "temp_expense_meta_id", nullable = false)
	private Long tempExpenseMetaId;

	@Column(name = "file_id")
	private Long fileId;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "merchantName", column = @Column(name = "merchant_name")),
		@AttributeOverride(name = "category", column = @Column(name = "category")),
		@AttributeOverride(
				name = "memo",
				column = @Column(name = "memo", columnDefinition = "TEXT")),
		@AttributeOverride(name = "occurredAt", column = @Column(name = "occurred_at"))
	})
	private TempExpenseContentInfo contentInfo;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(
				name = "localCurrencyCode",
				column = @Column(name = "local_country_code")),
		@AttributeOverride(
				name = "localCurrencyAmount",
				column = @Column(name = "local_currency_amount", precision = 10, scale = 2)),
		@AttributeOverride(name = "baseCurrencyCode", column = @Column(name = "base_country_code")),
		@AttributeOverride(
				name = "baseCurrencyAmount",
				column = @Column(name = "base_currency_amount", precision = 10, scale = 2)),
		@AttributeOverride(
				name = "exchangeRate",
				column = @Column(name = "exchange_rate", precision = 10, scale = 4))
	})
	private TempExpenseAmountInfo amountInfo;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(
				name = "cardLastFourDigits",
				column = @Column(name = "card_last_four_digits", length = 4)),
		@AttributeOverride(name = "approvalNumber", column = @Column(name = "approval_number"))
	})
	private TempExpensePaymentInfo paymentInfo;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TemporaryExpenseStatus status;

	public void applyPatch(TempExpensePatch patch, TempExpenseStatusPolicy statusPolicy) {
		if (patch == null || !patch.hasAnyChange()) {
			return;
		}
		this.contentInfo = getContentInfoOrEmpty();
		this.amountInfo = getAmountInfoOrEmpty();
		this.paymentInfo = getPaymentInfoOrEmpty();

		this.contentInfo =
				this.contentInfo.merge(
						patch.merchantName(), patch.category(), patch.memo(), patch.occurredAt());
		this.amountInfo =
				this.amountInfo.merge(
						patch.localCurrencyCode(),
						patch.localCurrencyAmount(),
						patch.baseCurrencyCode(),
						patch.baseCurrencyAmount(),
						patch.exchangeRate());
		this.paymentInfo =
				this.paymentInfo.merge(patch.cardLastFourDigits(), patch.approvalNumber());
		this.status = statusPolicy.resolve(this.contentInfo, this.amountInfo);
	}

	public TempExpenseContentInfo getContentInfoOrEmpty() {
		return contentInfo != null ? contentInfo : TempExpenseContentInfo.empty();
	}

	public TempExpenseAmountInfo getAmountInfoOrEmpty() {
		return amountInfo != null ? amountInfo : TempExpenseAmountInfo.empty();
	}

	public TempExpensePaymentInfo getPaymentInfoOrEmpty() {
		return paymentInfo != null ? paymentInfo : TempExpensePaymentInfo.empty();
	}

	// Legacy accessors to keep existing service/query code stable during 1st refactor step.
	public String getMerchantName() {
		return contentInfo != null ? contentInfo.getMerchantName() : null;
	}

	public Category getCategory() {
		return contentInfo != null ? contentInfo.getCategory() : null;
	}

	public String getMemo() {
		return contentInfo != null ? contentInfo.getMemo() : null;
	}

	public LocalDateTime getOccurredAt() {
		return contentInfo != null ? contentInfo.getOccurredAt() : null;
	}

	public CurrencyCode getLocalCountryCode() {
		return amountInfo != null ? amountInfo.getLocalCurrencyCode() : null;
	}

	public BigDecimal getLocalCurrencyAmount() {
		return amountInfo != null ? amountInfo.getLocalCurrencyAmount() : null;
	}

	public CurrencyCode getBaseCountryCode() {
		return amountInfo != null ? amountInfo.getBaseCurrencyCode() : null;
	}

	public BigDecimal getBaseCurrencyAmount() {
		return amountInfo != null ? amountInfo.getBaseCurrencyAmount() : null;
	}

	public BigDecimal getExchangeRate() {
		return amountInfo != null ? amountInfo.getExchangeRate() : null;
	}

	public String getCardLastFourDigits() {
		return paymentInfo != null ? paymentInfo.getCardLastFourDigits() : null;
	}

	public String getApprovalNumber() {
		return paymentInfo != null ? paymentInfo.getApprovalNumber() : null;
	}

	// Legacy builder for compatibility with existing creation paths.
	public static LegacyBuilder builder() {
		return new LegacyBuilder();
	}

	public static class LegacyBuilder {
		private Long tempExpenseId;
		private Long tempExpenseMetaId;
		private Long fileId;
		private String merchantName;
		private Category category;
		private CurrencyCode localCountryCode;
		private BigDecimal localCurrencyAmount;
		private CurrencyCode baseCountryCode;
		private BigDecimal baseCurrencyAmount;
		private BigDecimal exchangeRate;
		private String memo;
		private LocalDateTime occurredAt;
		private TemporaryExpenseStatus status;
		private String cardLastFourDigits;
		private String approvalNumber;

		public LegacyBuilder tempExpenseId(Long tempExpenseId) {
			this.tempExpenseId = tempExpenseId;
			return this;
		}

		public LegacyBuilder tempExpenseMetaId(Long tempExpenseMetaId) {
			this.tempExpenseMetaId = tempExpenseMetaId;
			return this;
		}

		public LegacyBuilder fileId(Long fileId) {
			this.fileId = fileId;
			return this;
		}

		public LegacyBuilder merchantName(String merchantName) {
			this.merchantName = merchantName;
			return this;
		}

		public LegacyBuilder category(Category category) {
			this.category = category;
			return this;
		}

		public LegacyBuilder localCountryCode(CurrencyCode localCountryCode) {
			this.localCountryCode = localCountryCode;
			return this;
		}

		public LegacyBuilder localCurrencyAmount(BigDecimal localCurrencyAmount) {
			this.localCurrencyAmount = localCurrencyAmount;
			return this;
		}

		public LegacyBuilder baseCountryCode(CurrencyCode baseCountryCode) {
			this.baseCountryCode = baseCountryCode;
			return this;
		}

		public LegacyBuilder baseCurrencyAmount(BigDecimal baseCurrencyAmount) {
			this.baseCurrencyAmount = baseCurrencyAmount;
			return this;
		}

		public LegacyBuilder exchangeRate(BigDecimal exchangeRate) {
			this.exchangeRate = exchangeRate;
			return this;
		}

		public LegacyBuilder memo(String memo) {
			this.memo = memo;
			return this;
		}

		public LegacyBuilder occurredAt(LocalDateTime occurredAt) {
			this.occurredAt = occurredAt;
			return this;
		}

		public LegacyBuilder status(TemporaryExpenseStatus status) {
			this.status = status;
			return this;
		}

		public LegacyBuilder cardLastFourDigits(String cardLastFourDigits) {
			this.cardLastFourDigits = cardLastFourDigits;
			return this;
		}

		public LegacyBuilder approvalNumber(String approvalNumber) {
			this.approvalNumber = approvalNumber;
			return this;
		}

		public TemporaryExpense build() {
			TemporaryExpense entity = new TemporaryExpense();
			entity.tempExpenseId = this.tempExpenseId;
			entity.tempExpenseMetaId = this.tempExpenseMetaId;
			entity.fileId = this.fileId;
			entity.contentInfo =
					TempExpenseContentInfo.of(
							this.merchantName, this.category, this.memo, this.occurredAt);
			entity.amountInfo =
					TempExpenseAmountInfo.of(
							this.localCountryCode,
							this.localCurrencyAmount,
							this.baseCountryCode,
							this.baseCurrencyAmount,
							this.exchangeRate);
			entity.paymentInfo =
					TempExpensePaymentInfo.of(this.cardLastFourDigits, this.approvalNumber);
			entity.status = this.status;
			return entity;
		}
	}
}
