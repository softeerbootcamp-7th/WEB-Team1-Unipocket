package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
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
		name = "pair_monthly_category_aggregate",
		indexes = {
			@Index(
					name = "idx_pair_monthly_category_lookup",
					columnList =
							"local_country_code,base_country_code,target_year_month,quality_type,currency_type")
		},
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_pair_monthly_category_aggregate",
					columnNames = {
						"local_country_code",
						"base_country_code",
						"target_year_month",
						"quality_type",
						"currency_type",
						"category"
					})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PairMonthlyCategoryAggregateEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	@Column(nullable = false, name = "local_country_code", length = 8)
	@Enumerated(EnumType.STRING)
	private CountryCode localCountryCode;

	@Column(nullable = false, name = "base_country_code", length = 8)
	@Enumerated(EnumType.STRING)
	private CountryCode baseCountryCode;

	@Column(nullable = false, name = "target_year_month")
	private LocalDate targetYearMonth;

	@Column(nullable = false, name = "quality_type", length = 16)
	@Enumerated(EnumType.STRING)
	private AnalysisQualityType qualityType;

	@Column(nullable = false, name = "currency_type", length = 16)
	@Enumerated(EnumType.STRING)
	private CurrencyType currencyType;

	@Column(nullable = false, name = "category", length = 32)
	@Enumerated(EnumType.STRING)
	private Category category;

	@Column(nullable = false, name = "included_account_count")
	private long includedAccountCount;

	@Column(nullable = false, name = "total_amount", precision = 19, scale = 4)
	private BigDecimal totalAmount;

	@Column(nullable = false, name = "average_amount", precision = 19, scale = 4)
	private BigDecimal averageAmount;

	public static PairMonthlyCategoryAggregateEntity of(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate targetYearMonth,
			AnalysisQualityType qualityType,
			CurrencyType currencyType,
			Category category,
			long includedAccountCount,
			BigDecimal totalAmount,
			BigDecimal averageAmount) {
		return PairMonthlyCategoryAggregateEntity.builder()
				.localCountryCode(localCountryCode)
				.baseCountryCode(baseCountryCode)
				.targetYearMonth(targetYearMonth)
				.qualityType(qualityType)
				.currencyType(currencyType)
				.category(category)
				.includedAccountCount(includedAccountCount)
				.totalAmount(totalAmount)
				.averageAmount(averageAmount)
				.build();
	}
}
