package com.genesis.unipocket.analysis.query.facade.port;

public interface AccountBookOwnershipValidator {

	void validateOwnership(Long accountBookId, String userId);
}
