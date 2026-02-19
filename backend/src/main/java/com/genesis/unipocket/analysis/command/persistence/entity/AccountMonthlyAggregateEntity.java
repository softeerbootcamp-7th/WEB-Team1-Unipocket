package com.genesis.unipocket.analysis.command.persistence.entity;

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
		name = "account_monthly_aggregate",
		indexes = {
			@Index(
					name = "idx_account_monthly_aggregate_country_month",
					columnList = "country_code,target_year_month,quality_type,metric_type")
		},
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_account_monthly_aggregate",
					columnNames = {
						"account_book_id",
						"target_year_month",
						"metric_type",
						"quality_type"
					})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountMonthlyAggregateEntity extends BaseEntity {

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

	@Column(nullable = false, name = "metric_type", length = 32)
	@Enumerated(EnumType.STRING)
	private AnalysisMetricType metricType;

	@Column(nullable = false, name = "quality_type", length = 16)
	@Enumerated(EnumType.STRING)
	private AnalysisQualityType qualityType;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal metricValue;

	public static AccountMonthlyAggregateEntity of(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate targetYearMonth,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal metricValue) {
		return AccountMonthlyAggregateEntity.builder()
				.accountBookId(accountBookId)
				.countryCode(countryCode)
				.targetYearMonth(targetYearMonth)
				.metricType(metricType)
				.qualityType(qualityType)
				.metricValue(metricValue)
				.build();
	}

	public void updateMetricValue(BigDecimal metricValue) {
		this.metricValue = metricValue;
	}
}
