package com.genesis.unipocket.analysis.query.port;

public interface AccountBookOwnershipValidator {

	void validateOwnership(Long accountBookId, String userId);
}
