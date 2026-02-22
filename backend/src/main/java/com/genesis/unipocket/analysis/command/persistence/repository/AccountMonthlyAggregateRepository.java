package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountMonthlyAggregateRepository
		extends JpaRepository<AccountMonthlyAggregateEntity, Long> {

	Optional<AccountMonthlyAggregateEntity>
			findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
					Long accountBookId,
					LocalDate targetYearMonth,
					AnalysisMetricType metricType,
					AnalysisQualityType qualityType);

	@Query(
			"""
			SELECT DISTINCT a.targetYearMonth
			FROM AccountMonthlyAggregateEntity a
			WHERE a.accountBookId = :accountBookId
			""")
	List<LocalDate> findDistinctTargetYearMonthsByAccountBookId(
			@Param("accountBookId") Long accountBookId);

	@Query(
			"""
			SELECT COALESCE(SUM(a.metricValue), 0)
			FROM AccountMonthlyAggregateEntity a
			WHERE a.accountBookId = :accountBookId
				AND a.metricType = :metricType
				AND a.qualityType = :qualityType
			""")
	BigDecimal sumMetricValueByAccountBookIdAndMetricTypeAndQualityType(
			@Param("accountBookId") Long accountBookId,
			@Param("metricType") AnalysisMetricType metricType,
			@Param("qualityType") AnalysisQualityType qualityType);

	@Query(
			"""
			SELECT COALESCE(SUM(a.metricValue), 0)
			FROM AccountMonthlyAggregateEntity a
			WHERE a.accountBookId = :accountBookId
				AND a.targetYearMonth IN :targetYearMonths
				AND a.metricType = :metricType
				AND a.qualityType = :qualityType
			""")
	BigDecimal sumMetricValueByAccountBookIdAndTargetYearMonthInAndMetricTypeAndQualityType(
			@Param("accountBookId") Long accountBookId,
			@Param("targetYearMonths") List<LocalDate> targetYearMonths,
			@Param("metricType") AnalysisMetricType metricType,
			@Param("qualityType") AnalysisQualityType qualityType);

	void deleteByAccountBookId(Long accountBookId);
}
