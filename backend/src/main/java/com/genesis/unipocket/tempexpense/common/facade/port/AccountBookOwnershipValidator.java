package com.genesis.unipocket.tempexpense.common.facade.port;

public interface AccountBookOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
