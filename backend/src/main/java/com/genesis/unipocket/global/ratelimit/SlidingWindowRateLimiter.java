package com.genesis.unipocket.global.ratelimit;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.exception.RateLimitExceededException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SlidingWindowRateLimiter {

	private static final Duration WINDOW = Duration.ofMinutes(1);
	private static final long MIN_RETRY_AFTER_SECONDS = 1L;

	private final Map<String, Deque<Long>> requestsByKey = new ConcurrentHashMap<>();

	public void validate(String bucket, String subject, int limit, ErrorCode errorCode) {
		String key = bucket + ":" + subject;
		Deque<Long> requestTimes = requestsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());
		long now = System.currentTimeMillis();
		long windowStart = now - WINDOW.toMillis();

		synchronized (requestTimes) {
			while (!requestTimes.isEmpty() && requestTimes.peekFirst() <= windowStart) {
				requestTimes.pollFirst();
			}

			if (requestTimes.size() >= limit) {
				long oldest = requestTimes.peekFirst();
				long retryAfterMillis = (oldest + WINDOW.toMillis()) - now;
				long retryAfterSeconds =
						Math.max(
								MIN_RETRY_AFTER_SECONDS,
								(long) Math.ceil(retryAfterMillis / 1000.0));
				throw new RateLimitExceededException(errorCode, retryAfterSeconds);
			}

			requestTimes.addLast(now);
		}
	}
}
