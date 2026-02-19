package com.genesis.unipocket.tempexpense.command.facade.port;

/**
 * <b>가계부 소유권 검증 포트</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public interface AccountBookOwnershipValidator {
	void validateOwnership(Long accountBookId, String userId);
}
