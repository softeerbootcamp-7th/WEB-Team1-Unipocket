package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyAnalysisResultEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryDailyAnalysisResultRepository
		extends JpaRepository<CountryDailyAnalysisResultEntity, Long> {

	Optional<CountryDailyAnalysisResultEntity>
			findByCountryCodeAndTargetLocalDateAndMetricTypeAndQualityType(
					CountryCode countryCode,
					LocalDate targetLocalDate,
					AnalysisMetricType metricType,
					AnalysisQualityType qualityType);
}
