package com.genesis.unipocket.expense.query.facade.port;

public interface AccountBookOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
