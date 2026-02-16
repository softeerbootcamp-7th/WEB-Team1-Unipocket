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

	@Query(
			"""
			SELECT d.id
			FROM AnalysisMonthlyDirtyEntity d
			WHERE d.countryCode = :countryCode
				AND d.status IN :statuses
				AND (d.nextRetryAtUtc IS NULL OR d.nextRetryAtUtc <= :nowUtc)
			ORDER BY d.targetYearMonth ASC, d.id ASC
			""")
	List<Long> findDispatchableIdsByCountryCode(
			@Param("countryCode") CountryCode countryCode,
			@Param("statuses") Collection<AnalysisBatchJobStatus> statuses,
			@Param("nowUtc") LocalDateTime nowUtc,
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
}
