package com.genesis.unipocket.expense.command.facade.port;

public interface AccountBookOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
