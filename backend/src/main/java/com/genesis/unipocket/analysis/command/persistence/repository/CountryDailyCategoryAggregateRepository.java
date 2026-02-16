package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyCategoryAggregateEntity;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryDailyCategoryAggregateRepository
		extends JpaRepository<CountryDailyCategoryAggregateEntity, Long> {

	void deleteByCountryCodeAndTargetLocalDateAndQualityType(
			CountryCode countryCode, LocalDate targetLocalDate, AnalysisQualityType qualityType);

	List<CountryDailyCategoryAggregateEntity>
			findAllByCountryCodeAndTargetLocalDateAndCategoryInAndQualityType(
					CountryCode countryCode,
					LocalDate targetLocalDate,
					Collection<Category> categories,
					AnalysisQualityType qualityType);
}
