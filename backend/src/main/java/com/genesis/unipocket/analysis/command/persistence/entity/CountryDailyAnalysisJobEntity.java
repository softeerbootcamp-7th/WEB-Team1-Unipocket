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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(
		name = "country_daily_analysis_job",
		indexes = {
			@Index(
					name = "idx_country_analysis_job_status_retry",
					columnList = "status,next_retry_at_utc")
		},
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_country_analysis_job_country_run_date",
					columnNames = {"country_code", "run_local_date"})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CountryDailyAnalysisJobEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, name = "job_id")
	private Long id;

	@Column(nullable = false, name = "country_code", length = 8)
	@Enumerated(EnumType.STRING)
	private CountryCode countryCode;

	@Column(nullable = false, name = "run_local_date")
	private LocalDate runLocalDate;

	@Column(nullable = false, name = "range_start_local_date")
	private LocalDate rangeStartLocalDate;

	@Column(nullable = false, name = "range_end_local_date")
	private LocalDate rangeEndLocalDate;

	@Column(nullable = false, length = 16)
	@Enumerated(EnumType.STRING)
	private AnalysisBatchJobStatus status;

	@Column(nullable = false)
	private int attemptCount;

	@Column(name = "next_retry_at_utc")
	private LocalDateTime nextRetryAtUtc;

	@Column(name = "picked_by", length = 64)
	private String pickedBy;

	@Column(name = "lease_until_utc")
	private LocalDateTime leaseUntilUtc;

	@Column(name = "started_at_utc")
	private LocalDateTime startedAtUtc;

	@Column(name = "finished_at_utc")
	private LocalDateTime finishedAtUtc;

	@Column(name = "error_code", length = 64)
	private String errorCode;

	@Column(name = "error_message", length = 1000)
	private String errorMessage;

	public static CountryDailyAnalysisJobEntity create(
			CountryCode countryCode,
			LocalDate runLocalDate,
			LocalDate rangeStartLocalDate,
			LocalDate rangeEndLocalDate) {
		return CountryDailyAnalysisJobEntity.builder()
				.countryCode(countryCode)
				.runLocalDate(runLocalDate)
				.rangeStartLocalDate(rangeStartLocalDate)
				.rangeEndLocalDate(rangeEndLocalDate)
				.status(AnalysisBatchJobStatus.PENDING)
				.attemptCount(0)
				.build();
	}

	public void markRunning(String pickedBy, LocalDateTime nowUtc, LocalDateTime leaseUntilUtc) {
		this.status = AnalysisBatchJobStatus.RUNNING;
		this.attemptCount += 1;
		this.pickedBy = pickedBy;
		this.leaseUntilUtc = leaseUntilUtc;
		if (this.startedAtUtc == null) {
			this.startedAtUtc = nowUtc;
		}
		this.nextRetryAtUtc = null;
		this.errorCode = null;
		this.errorMessage = null;
	}

	public void markSuccess(LocalDateTime nowUtc) {
		this.status = AnalysisBatchJobStatus.SUCCESS;
		this.finishedAtUtc = nowUtc;
		this.nextRetryAtUtc = null;
		this.leaseUntilUtc = null;
		this.errorCode = null;
		this.errorMessage = null;
	}

	public void markRetry(LocalDateTime retryAtUtc, String errorCode, String errorMessage) {
		this.status = AnalysisBatchJobStatus.RETRY;
		this.nextRetryAtUtc = retryAtUtc;
		this.leaseUntilUtc = null;
		this.errorCode = errorCode;
		this.errorMessage = trimError(errorMessage);
	}

	public void markDead(LocalDateTime nowUtc, String errorCode, String errorMessage) {
		this.status = AnalysisBatchJobStatus.DEAD;
		this.finishedAtUtc = nowUtc;
		this.nextRetryAtUtc = null;
		this.leaseUntilUtc = null;
		this.errorCode = errorCode;
		this.errorMessage = trimError(errorMessage);
	}

	private static String trimError(String message) {
		if (message == null) {
			return null;
		}
		return message.length() > 1000 ? message.substring(0, 1000) : message;
	}
}
