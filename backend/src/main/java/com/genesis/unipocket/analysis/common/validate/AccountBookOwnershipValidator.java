package com.genesis.unipocket.analysis.common.validate;

public interface AccountBookOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
