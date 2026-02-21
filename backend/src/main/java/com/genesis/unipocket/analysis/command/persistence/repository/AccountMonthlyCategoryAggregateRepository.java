package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountMonthlyCategoryAggregateRepository
		extends JpaRepository<AccountMonthlyCategoryAggregateEntity, Long> {

	void deleteByAccountBookIdAndTargetYearMonthAndQualityType(
			Long accountBookId, LocalDate targetYearMonth, AnalysisQualityType qualityType);

	@Query(
			"""
			SELECT DISTINCT a.targetYearMonth
			FROM AccountMonthlyCategoryAggregateEntity a
			WHERE a.accountBookId = :accountBookId
			""")
	List<LocalDate> findDistinctTargetYearMonthsByAccountBookId(
			@Param("accountBookId") Long accountBookId);

	void deleteByAccountBookId(Long accountBookId);
}
