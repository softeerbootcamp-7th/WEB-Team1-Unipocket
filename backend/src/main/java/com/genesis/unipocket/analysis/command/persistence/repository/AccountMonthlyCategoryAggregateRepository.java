package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMonthlyCategoryAggregateRepository
		extends JpaRepository<AccountMonthlyCategoryAggregateEntity, Long> {

	void deleteByAccountBookIdAndTargetYearMonthAndQualityType(
			Long accountBookId, LocalDate targetYearMonth, AnalysisQualityType qualityType);
}
