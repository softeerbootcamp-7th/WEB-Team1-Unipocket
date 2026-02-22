package com.genesis.unipocket.expense.common.validation;

public interface ExpenseOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
