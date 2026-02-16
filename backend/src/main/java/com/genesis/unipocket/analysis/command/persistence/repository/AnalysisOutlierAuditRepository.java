package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisOutlierAuditEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisOutlierAuditRepository
		extends JpaRepository<AnalysisOutlierAuditEntity, Long> {

	void deleteByCountryCodeAndOccurredDate(CountryCode countryCode, LocalDate occurredDate);
}
