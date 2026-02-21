package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairMonthlyAggregateRepository
		extends JpaRepository<PairMonthlyAggregateEntity, Long> {

	Optional<PairMonthlyAggregateEntity>
			findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
					CountryCode localCountryCode,
					CountryCode baseCountryCode,
					LocalDate targetYearMonth,
					AnalysisQualityType qualityType,
					AnalysisMetricType metricType);

	void deleteByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonth(
			CountryCode localCountryCode, CountryCode baseCountryCode, LocalDate targetYearMonth);
}
