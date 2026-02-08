package com.genesis.unipocket.auth;

import com.genesis.unipocket.accountbook.service.AccountBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <b>가계부 접근 권한 검증</b>
 *
 * <p>
 * 현재 사용자가 요청한 가계부에 접근 권한이 있는지 검증
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountBookAccessValidator {

	private final AccountBookService accountBookService;
	private final AuthenticationContext authenticationContext;

	/**
	 * 가계부 접근 권한 검증
	 *
	 * @param accountBookId 가계부 ID
	 * @throws UnauthorizedException    인증되지 않은 사용자
	 * @throws ForbiddenException       접근 권한이 없는 경우
	 * @throws IllegalArgumentException 가계부를 찾을 수 없는 경우
	 */
	public void validateAccess(Long accountBookId) {
		// 1. 인증 확인
		String currentUserId = authenticationContext.getCurrentUserIdAsString();

		// 2. AccountBookService를 통해 가계부 조회 및 소유권 검증
		// getAccountBook 내부에서 findAndVerifyOwnership을 호출하여 소유권을 자동으로 검증
		try {
			accountBookService.getAccountBook(accountBookId, currentUserId);
			log.debug(
					"Access granted for user {} to account book {}", currentUserId, accountBookId);
		} catch (com.genesis.unipocket.global.exception.BusinessException e) {
			// BusinessException을 적절한 예외로 변환
			if (e.getCode()
					== com.genesis.unipocket.global.exception.ErrorCode.ACCOUNT_BOOK_NOT_FOUND) {
				throw new IllegalArgumentException("가계부를 찾을 수 없습니다.");
			} else if (e.getCode()
					== com.genesis.unipocket.global.exception.ErrorCode
							.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS) {
				log.warn(
						"User {} attempted to access account book {} without permission",
						currentUserId,
						accountBookId);
				throw new ForbiddenException("해당 가계부에 접근 권한이 없습니다.");
			}
			throw e;
		}
	}

	/**
	 * 가계부 소유자 확인 (예외를 던지지 않음)
	 *
	 * @param accountBookId 가계부 ID
	 * @return 소유자인 경우 true
	 */
	public boolean isOwner(Long accountBookId) {
		try {
			validateAccess(accountBookId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
