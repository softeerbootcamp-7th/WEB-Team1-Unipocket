package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyAnalysisJobEntity;
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

public interface CountryDailyAnalysisJobRepository
		extends JpaRepository<CountryDailyAnalysisJobEntity, Long> {

	Optional<CountryDailyAnalysisJobEntity> findByCountryCodeAndRunLocalDate(
			CountryCode countryCode, LocalDate runLocalDate);

	@Query(
			"""
			SELECT j.id
			FROM CountryDailyAnalysisJobEntity j
			WHERE j.status IN :statuses
				AND (j.nextRetryAtUtc IS NULL OR j.nextRetryAtUtc <= :nowUtc)
			ORDER BY j.runLocalDate ASC, j.id ASC
			""")
	List<Long> findDispatchableJobIds(
			@Param("statuses") Collection<AnalysisBatchJobStatus> statuses,
			@Param("nowUtc") LocalDateTime nowUtc,
			Pageable pageable);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
			"""
			UPDATE CountryDailyAnalysisJobEntity j
			SET j.status = :running,
				j.attemptCount = j.attemptCount + 1,
				j.pickedBy = :pickedBy,
				j.leaseUntilUtc = :leaseUntilUtc,
				j.startedAtUtc = CASE WHEN j.startedAtUtc IS NULL THEN :nowUtc ELSE j.startedAtUtc END,
				j.nextRetryAtUtc = NULL,
				j.errorCode = NULL,
				j.errorMessage = NULL
			WHERE j.id = :jobId
				AND j.status IN :claimableStatuses
				AND (j.nextRetryAtUtc IS NULL OR j.nextRetryAtUtc <= :nowUtc)
			""")
	int claimJob(
			@Param("jobId") Long jobId,
			@Param("running") AnalysisBatchJobStatus running,
			@Param("claimableStatuses") Collection<AnalysisBatchJobStatus> claimableStatuses,
			@Param("pickedBy") String pickedBy,
			@Param("nowUtc") LocalDateTime nowUtc,
			@Param("leaseUntilUtc") LocalDateTime leaseUntilUtc);
}
