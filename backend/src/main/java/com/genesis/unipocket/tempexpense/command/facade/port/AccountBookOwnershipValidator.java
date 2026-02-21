package com.genesis.unipocket.tempexpense.command.facade.port;

public interface AccountBookOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
