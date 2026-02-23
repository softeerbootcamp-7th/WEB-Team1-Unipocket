package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisMonthlyDirtyRepository
		extends JpaRepository<AnalysisMonthlyDirtyEntity, Long> {

	Optional<AnalysisMonthlyDirtyEntity> findByCountryCodeAndAccountBookIdAndTargetYearMonth(
			CountryCode countryCode, Long accountBookId, LocalDate targetYearMonth);

	boolean existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
			CountryCode countryCode,
			Long accountBookId,
			LocalDate targetYearMonth,
			AnalysisBatchJobStatus status);

	boolean existsByCountryCodeAndAccountBookIdAndStatusNot(
			CountryCode countryCode, Long accountBookId, AnalysisBatchJobStatus status);

	@Query(
			"""
			SELECT d.targetYearMonth
			FROM AnalysisMonthlyDirtyEntity d
			WHERE d.countryCode = :countryCode
				AND d.accountBookId = :accountBookId
				AND d.status <> :status
			ORDER BY d.targetYearMonth ASC
			""")
	List<LocalDate> findTargetYearMonthsByCountryCodeAndAccountBookIdAndStatusNot(
			@Param("countryCode") CountryCode countryCode,
			@Param("accountBookId") Long accountBookId,
			@Param("status") AnalysisBatchJobStatus status);

	@Query(
			"""
			SELECT d.id
			FROM AnalysisMonthlyDirtyEntity d
			WHERE d.countryCode = :countryCode
				AND d.status IN :statuses
				AND (d.nextRetryAtUtc IS NULL OR d.nextRetryAtUtc <= :nowUtc)
				AND d.lastEventAtUtc <= :batchStartUtc
			ORDER BY d.targetYearMonth ASC, d.id ASC
			""")
	List<Long> findDispatchableIdsByCountryCode(
			@Param("countryCode") CountryCode countryCode,
			@Param("statuses") Collection<AnalysisBatchJobStatus> statuses,
			@Param("nowUtc") LocalDateTime nowUtc,
			@Param("batchStartUtc") LocalDateTime batchStartUtc,
			Pageable pageable);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
			"""
			UPDATE AnalysisMonthlyDirtyEntity d
			SET d.status = :running,
				d.attemptCount = d.attemptCount + 1,
				d.leaseUntilUtc = :leaseUntilUtc,
				d.nextRetryAtUtc = NULL,
				d.errorCode = NULL,
				d.errorMessage = NULL
			WHERE d.id = :id
				AND d.status IN :claimableStatuses
				AND (d.nextRetryAtUtc IS NULL OR d.nextRetryAtUtc <= :nowUtc)
			""")
	int claimDirty(
			@Param("id") Long id,
			@Param("running") AnalysisBatchJobStatus running,
			@Param("claimableStatuses") Collection<AnalysisBatchJobStatus> claimableStatuses,
			@Param("nowUtc") LocalDateTime nowUtc,
			@Param("leaseUntilUtc") LocalDateTime leaseUntilUtc);

	void deleteByAccountBookId(Long accountBookId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
			"""
			UPDATE AnalysisMonthlyDirtyEntity d
			SET d.status = :pending,
				d.nextRetryAtUtc = NULL,
				d.leaseUntilUtc = NULL,
				d.errorCode = NULL,
				d.errorMessage = NULL
			WHERE d.id = :id
				AND d.status = :running
				AND d.lastEventAtUtc > :claimTime
			""")
	int markPendingIfNewEventDuringRun(
			@Param("id") Long id,
			@Param("running") AnalysisBatchJobStatus running,
			@Param("pending") AnalysisBatchJobStatus pending,
			@Param("claimTime") LocalDateTime claimTime);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
			"""
			UPDATE AnalysisMonthlyDirtyEntity d
			SET d.status = :success,
				d.nextRetryAtUtc = NULL,
				d.leaseUntilUtc = NULL,
				d.errorCode = NULL,
				d.errorMessage = NULL,
				d.lastEventAtUtc = :completedAt
			WHERE d.id = :id
				AND d.status = :running
				AND d.lastEventAtUtc <= :claimTime
			""")
	int markSuccessIfNoNewEventDuringRun(
			@Param("id") Long id,
			@Param("running") AnalysisBatchJobStatus running,
			@Param("success") AnalysisBatchJobStatus success,
			@Param("claimTime") LocalDateTime claimTime,
			@Param("completedAt") LocalDateTime completedAt);
}
