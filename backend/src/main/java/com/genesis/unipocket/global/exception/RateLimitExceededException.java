package com.genesis.unipocket.global.exception;

import lombok.Getter;

/**
 * <b>요청 한도 초과 예외</b>
 */
@Getter
public class RateLimitExceededException extends BusinessException {

	private final long retryAfterSeconds;

	public RateLimitExceededException(ErrorCode code, long retryAfterSeconds) {
		super(code);
		this.retryAfterSeconds = retryAfterSeconds;
	}
}
