package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMonthlyAggregateRepository
		extends JpaRepository<AccountMonthlyAggregateEntity, Long> {

	Optional<AccountMonthlyAggregateEntity>
			findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
					Long accountBookId,
					LocalDate targetYearMonth,
					AnalysisMetricType metricType,
					AnalysisQualityType qualityType);

	boolean existsByCountryCodeAndTargetYearMonthAndMetricTypeAndQualityType(
			CountryCode countryCode,
			LocalDate targetYearMonth,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType);
}
