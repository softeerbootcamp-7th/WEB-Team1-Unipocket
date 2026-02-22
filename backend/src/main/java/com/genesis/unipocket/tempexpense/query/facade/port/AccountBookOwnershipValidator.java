package com.genesis.unipocket.tempexpense.query.facade.port;

public interface AccountBookOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
