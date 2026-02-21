package com.genesis.unipocket.expense.query.service;

import com.genesis.unipocket.expense.query.persistence.repository.ExpenseQueryDslRepository;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseOneShotRow;
import com.genesis.unipocket.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseQueryService {

	private final ExpenseQueryDslRepository expenseQueryRepository;

	public ExpenseOneShotRow getExpenseOneShot(Long expenseId, Long accountBookId) {
		return expenseQueryRepository
				.findExpenseOneShot(accountBookId, expenseId)
				.orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));
	}

	public Page<ExpenseOneShotRow> getExpensesOneShot(
			Long accountBookId, ExpenseSearchFilter filter, Pageable pageable) {
		return expenseQueryRepository.findExpensesOneShot(accountBookId, filter, pageable);
	}

	public List<String> searchMerchantNames(Long accountBookId, String query, Integer limit) {

		if (query == null || query.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		int pageSize = limit == null ? 10 : limit;
		if (pageSize < 1 || pageSize > 20) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		String normalizedQuery = query.trim();
		return expenseQueryRepository.findMerchantNameSuggestions(
				accountBookId, normalizedQuery, pageSize);
	}
}
