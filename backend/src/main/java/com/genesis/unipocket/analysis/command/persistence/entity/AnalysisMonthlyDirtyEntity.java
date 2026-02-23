package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
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
		name = "analysis_monthly_dirty",
		indexes = {
			@Index(
					name = "idx_analysis_monthly_dirty_status_retry",
					columnList = "status,next_retry_at_utc,country_code")
		},
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_analysis_monthly_dirty",
					columnNames = {"country_code", "account_book_id", "target_year_month"})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisMonthlyDirtyEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	@Column(nullable = false, name = "country_code", length = 8)
	@Enumerated(EnumType.STRING)
	private CountryCode countryCode;

	@Column(nullable = false, name = "account_book_id")
	private Long accountBookId;

	@Column(nullable = false, name = "target_year_month")
	private LocalDate targetYearMonth;

	@Column(nullable = false, length = 16)
	@Enumerated(EnumType.STRING)
	private AnalysisBatchJobStatus status;

	@Column(nullable = false)
	private int attemptCount;

	@Column(name = "next_retry_at_utc")
	private LocalDateTime nextRetryAtUtc;

	@Column(name = "lease_until_utc")
	private LocalDateTime leaseUntilUtc;

	@Column(name = "last_event_at_utc", nullable = false)
	private LocalDateTime lastEventAtUtc;

	@Column(name = "error_code", length = 64)
	private String errorCode;

	@Column(name = "error_message", length = 1000)
	private String errorMessage;

	public static AnalysisMonthlyDirtyEntity create(
			CountryCode countryCode,
			Long accountBookId,
			LocalDate targetYearMonth,
			LocalDateTime nowUtc) {
		return AnalysisMonthlyDirtyEntity.builder()
				.countryCode(countryCode)
				.accountBookId(accountBookId)
				.targetYearMonth(targetYearMonth)
				.status(AnalysisBatchJobStatus.PENDING)
				.attemptCount(0)
				.lastEventAtUtc(nowUtc)
				.build();
	}

	public void markPendingFromEvent(LocalDateTime nowUtc) {
		if (this.status != AnalysisBatchJobStatus.RUNNING) {
			this.status = AnalysisBatchJobStatus.PENDING;
			this.nextRetryAtUtc = null;
			this.leaseUntilUtc = null;
			this.errorCode = null;
			this.errorMessage = null;
		}
		this.lastEventAtUtc = nowUtc;
	}

	public void markSuccess(LocalDateTime nowUtc) {
		this.status = AnalysisBatchJobStatus.SUCCESS;
		this.nextRetryAtUtc = null;
		this.leaseUntilUtc = null;
		this.errorCode = null;
		this.errorMessage = null;
		this.lastEventAtUtc = nowUtc;
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
		this.nextRetryAtUtc = null;
		this.leaseUntilUtc = null;
		this.errorCode = errorCode;
		this.errorMessage = trimError(errorMessage);
		this.lastEventAtUtc = nowUtc;
	}

	private static String trimError(String message) {
		if (message == null) {
			return null;
		}
		return message.length() > 1000 ? message.substring(0, 1000) : message;
	}
}
