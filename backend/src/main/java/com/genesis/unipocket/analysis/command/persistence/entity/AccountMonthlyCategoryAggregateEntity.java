package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.global.common.entity.BaseEntity;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(
		name = "account_monthly_category_aggregate",
		indexes = {
			@Index(
					name = "idx_account_monthly_category_country_month",
					columnList = "country_code,target_year_month,quality_type")
		},
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_account_monthly_category_aggregate",
					columnNames = {
						"account_book_id",
						"target_year_month",
						"category",
						"quality_type"
					})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountMonthlyCategoryAggregateEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	@Column(nullable = false, name = "account_book_id")
	private Long accountBookId;

	@Column(nullable = false, name = "country_code", length = 8)
	@Enumerated(EnumType.STRING)
	private CountryCode countryCode;

	@Column(nullable = false, name = "target_year_month")
	private LocalDate targetYearMonth;

	@Column(nullable = false, name = "category", length = 32)
	@Enumerated(EnumType.STRING)
	private Category category;

	@Column(nullable = false, name = "quality_type", length = 16)
	@Enumerated(EnumType.STRING)
	private AnalysisQualityType qualityType;

	@Column(nullable = false, name = "total_amount", precision = 19, scale = 4)
	private BigDecimal totalAmount;

	@Column(nullable = false, name = "expense_count")
	private long expenseCount;

	public static AccountMonthlyCategoryAggregateEntity of(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate targetYearMonth,
			Category category,
			AnalysisQualityType qualityType,
			BigDecimal totalAmount,
			long expenseCount) {
		return AccountMonthlyCategoryAggregateEntity.builder()
				.accountBookId(accountBookId)
				.countryCode(countryCode)
				.targetYearMonth(targetYearMonth)
				.category(category)
				.qualityType(qualityType)
				.totalAmount(totalAmount)
				.expenseCount(expenseCount)
				.build();
	}
}
