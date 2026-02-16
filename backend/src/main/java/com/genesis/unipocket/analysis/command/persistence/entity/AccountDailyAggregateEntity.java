package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
		name = "account_daily_aggregate",
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_account_daily_aggregate",
					columnNames = {
						"account_book_id",
						"target_local_date",
						"metric_type",
						"quality_type"
					})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountDailyAggregateEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	@Column(nullable = false, name = "account_book_id")
	private Long accountBookId;

	@Column(nullable = false, name = "target_local_date")
	private LocalDate targetLocalDate;

	@Column(nullable = false, name = "metric_type", length = 32)
	@Enumerated(EnumType.STRING)
	private AnalysisMetricType metricType;

	@Column(nullable = false, name = "quality_type", length = 16)
	@Enumerated(EnumType.STRING)
	private AnalysisQualityType qualityType;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal metricValue;

	public static AccountDailyAggregateEntity of(
			Long accountBookId,
			LocalDate targetLocalDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal metricValue) {
		return AccountDailyAggregateEntity.builder()
				.accountBookId(accountBookId)
				.targetLocalDate(targetLocalDate)
				.metricType(metricType)
				.qualityType(qualityType)
				.metricValue(metricValue)
				.build();
	}

	public void updateMetricValue(BigDecimal metricValue) {
		this.metricValue = metricValue;
	}
}
