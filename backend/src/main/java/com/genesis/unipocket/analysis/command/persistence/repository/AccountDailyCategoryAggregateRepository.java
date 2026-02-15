package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountDailyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.Category;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDailyCategoryAggregateRepository
		extends JpaRepository<AccountDailyCategoryAggregateEntity, Long> {

	void deleteByAccountBookIdInAndTargetLocalDateAndQualityType(
			Collection<Long> accountBookIds,
			LocalDate targetLocalDate,
			AnalysisQualityType qualityType);

	List<AccountDailyCategoryAggregateEntity>
			findAllByAccountBookIdInAndTargetLocalDateAndCategoryInAndQualityType(
					Collection<Long> accountBookIds,
					LocalDate targetLocalDate,
					Collection<Category> categories,
					AnalysisQualityType qualityType);
}
