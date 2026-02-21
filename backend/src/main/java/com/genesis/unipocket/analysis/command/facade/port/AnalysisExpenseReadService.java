package com.genesis.unipocket.analysis.command.facade.port;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface AnalysisExpenseReadService {

	Optional<ExpenseOccurredRange> findOccurredAtRange(Long accountBookId);

	record ExpenseOccurredRange(OffsetDateTime minOccurredAt, OffsetDateTime maxOccurredAt) {}
}
