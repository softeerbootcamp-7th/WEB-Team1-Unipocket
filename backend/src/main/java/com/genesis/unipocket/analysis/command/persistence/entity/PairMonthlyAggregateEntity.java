package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.global.common.entity.BaseEntity;
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
		name = "pair_monthly_aggregate",
		indexes = {
			@Index(
					name = "idx_pair_monthly_aggregate_lookup",
					columnList =
							"local_country_code,base_country_code,target_year_month,quality_type,metric_type")
		},
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_pair_monthly_aggregate",
					columnNames = {
						"local_country_code",
						"base_country_code",
						"target_year_month",
						"quality_type",
						"metric_type"
					})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PairMonthlyAggregateEntity extends BaseEntity {

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

	@Column(nullable = false, name = "metric_type", length = 32)
	@Enumerated(EnumType.STRING)
	private AnalysisMetricType metricType;

	@Column(nullable = false, name = "included_account_count")
	private long includedAccountCount;

	@Column(nullable = false, name = "total_metric_sum", precision = 19, scale = 4)
	private BigDecimal totalMetricSum;

	@Column(nullable = false, name = "average_metric_value", precision = 19, scale = 4)
	private BigDecimal averageMetricValue;

	@Column(name = "iqr_lower_bound", precision = 19, scale = 4)
	private BigDecimal iqrLowerBound;

	@Column(name = "iqr_upper_bound", precision = 19, scale = 4)
	private BigDecimal iqrUpperBound;

	public static PairMonthlyAggregateEntity of(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate targetYearMonth,
			AnalysisQualityType qualityType,
			AnalysisMetricType metricType,
			long includedAccountCount,
			BigDecimal totalMetricSum,
			BigDecimal averageMetricValue,
			BigDecimal iqrLowerBound,
			BigDecimal iqrUpperBound) {
		return PairMonthlyAggregateEntity.builder()
				.localCountryCode(localCountryCode)
				.baseCountryCode(baseCountryCode)
				.targetYearMonth(targetYearMonth)
				.qualityType(qualityType)
				.metricType(metricType)
				.includedAccountCount(includedAccountCount)
				.totalMetricSum(totalMetricSum)
				.averageMetricValue(averageMetricValue)
				.iqrLowerBound(iqrLowerBound)
				.iqrUpperBound(iqrUpperBound)
				.build();
	}

	public void update(
			long includedAccountCount,
			BigDecimal totalMetricSum,
			BigDecimal averageMetricValue,
			BigDecimal iqrLowerBound,
			BigDecimal iqrUpperBound) {
		this.includedAccountCount = includedAccountCount;
		this.totalMetricSum = totalMetricSum;
		this.averageMetricValue = averageMetricValue;
		this.iqrLowerBound = iqrLowerBound;
		this.iqrUpperBound = iqrUpperBound;
	}
}
