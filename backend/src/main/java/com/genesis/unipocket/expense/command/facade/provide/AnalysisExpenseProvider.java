package com.genesis.unipocket.expense.command.facade.provide;

import com.genesis.unipocket.analysis.command.facade.port.AnalysisExpenseReadService;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisExpenseProvider implements AnalysisExpenseReadService {

	private final ExpenseRepository expenseRepository;

	@Override
	public Optional<ExpenseOccurredRange> findOccurredAtRange(Long accountBookId) {
		Object[] occurredRange = expenseRepository.findOccurredAtRangeByAccountBookId(accountBookId);
		if (occurredRange == null || occurredRange.length < 2) {
			return Optional.empty();
		}
		OffsetDateTime minOccurredAt = toOffsetDateTime(occurredRange[0]);
		OffsetDateTime maxOccurredAt = toOffsetDateTime(occurredRange[1]);
		if (minOccurredAt == null || maxOccurredAt == null) {
			return Optional.empty();
		}
		return Optional.of(new ExpenseOccurredRange(minOccurredAt, maxOccurredAt));
	}

	private OffsetDateTime toOffsetDateTime(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof OffsetDateTime offsetDateTime) {
			return offsetDateTime;
		}
		if (value instanceof LocalDate localDate) {
			return localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
		}
		if (value instanceof Date sqlDate) {
			return sqlDate.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
		}
		throw new IllegalStateException("Unsupported occurredAt type: " + value.getClass().getName());
	}
}
