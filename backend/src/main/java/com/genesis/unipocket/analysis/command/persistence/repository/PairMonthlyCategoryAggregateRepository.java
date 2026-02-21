package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairMonthlyCategoryAggregateRepository
		extends JpaRepository<PairMonthlyCategoryAggregateEntity, Long> {

	void deleteByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate targetYearMonth,
			AnalysisQualityType qualityType,
			CurrencyType currencyType);

	List<PairMonthlyCategoryAggregateEntity>
			findAllByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
					CountryCode localCountryCode,
					CountryCode baseCountryCode,
					LocalDate targetYearMonth,
					AnalysisQualityType qualityType,
					CurrencyType currencyType);

    void deleteByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthIn(
            CountryCode localCountryCode, CountryCode baseCountryCode, java.util.Collection<LocalDate> targetYearMonths);
}
