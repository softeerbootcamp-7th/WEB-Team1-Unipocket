package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.ratelimit.SlidingWindowRateLimiter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <b>임시지출 업로드/파싱 요청 제한 서비스</b>
 */
@Service
public class TemporaryExpenseRateLimitService {

	private final int uploadMaxRequestsPerMinute;
	private final int parseMaxRequestsPerMinute;
	private final SlidingWindowRateLimiter slidingWindowRateLimiter;

	public TemporaryExpenseRateLimitService(
			SlidingWindowRateLimiter slidingWindowRateLimiter,
			@Value("${tempexpense.rate-limit.upload.max-requests-per-minute:20}")
					int uploadMaxRequestsPerMinute,
			@Value("${tempexpense.rate-limit.parse.max-requests-per-minute:5}")
					int parseMaxRequestsPerMinute) {
		this.slidingWindowRateLimiter = slidingWindowRateLimiter;
		this.uploadMaxRequestsPerMinute = uploadMaxRequestsPerMinute;
		this.parseMaxRequestsPerMinute = parseMaxRequestsPerMinute;
	}

	/** 업로드 URL 발급 요청 제한 */
	public void validateUploadRequest(UUID userId) {
		slidingWindowRateLimiter.validate(
				"tempexpense-upload",
				userId.toString(),
				uploadMaxRequestsPerMinute,
				ErrorCode.TEMP_EXPENSE_RATE_LIMIT_EXCEEDED);
	}

	/** 파싱 시작 요청 제한 */
	public void validateParseRequest(UUID userId) {
		slidingWindowRateLimiter.validate(
				"tempexpense-parse",
				userId.toString(),
				parseMaxRequestsPerMinute,
				ErrorCode.TEMP_EXPENSE_RATE_LIMIT_EXCEEDED);
	}
}
