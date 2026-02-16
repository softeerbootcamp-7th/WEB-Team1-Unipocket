package com.genesis.unipocket.expense.query.service;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.ratelimit.SlidingWindowRateLimiter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseMerchantSearchRateLimitService {

	private static final String RATE_LIMIT_BUCKET = "expense-merchant-search";
	private final SlidingWindowRateLimiter slidingWindowRateLimiter;

	@Value("${expense.rate-limit.merchant-search.max-requests-per-minute:60}")
	private int merchantSearchMaxRequestsPerMinute;

	public void validate(UUID userId) {
		slidingWindowRateLimiter.validate(
				RATE_LIMIT_BUCKET,
				userId.toString(),
				merchantSearchMaxRequestsPerMinute,
				ErrorCode.EXPENSE_MERCHANT_SEARCH_RATE_LIMIT_EXCEEDED);
	}
}
