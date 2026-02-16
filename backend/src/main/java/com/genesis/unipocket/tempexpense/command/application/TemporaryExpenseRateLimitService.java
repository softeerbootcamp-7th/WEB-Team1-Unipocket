package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.exception.RateLimitExceededException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <b>임시지출 업로드/파싱 요청 제한 서비스</b>
 */
@Service
public class TemporaryExpenseRateLimitService {

	private static final Duration WINDOW = Duration.ofMinutes(1);
	private static final long MIN_RETRY_AFTER_SECONDS = 1L;

	private final int uploadMaxRequestsPerMinute;
	private final int parseMaxRequestsPerMinute;

	private final Map<String, Deque<Long>> uploadRequestsByUser = new ConcurrentHashMap<>();
	private final Map<String, Deque<Long>> parseRequestsByUser = new ConcurrentHashMap<>();

	public TemporaryExpenseRateLimitService(
			@Value("${tempexpense.rate-limit.upload.max-requests-per-minute:20}")
					int uploadMaxRequestsPerMinute,
			@Value("${tempexpense.rate-limit.parse.max-requests-per-minute:5}")
					int parseMaxRequestsPerMinute) {
		this.uploadMaxRequestsPerMinute = uploadMaxRequestsPerMinute;
		this.parseMaxRequestsPerMinute = parseMaxRequestsPerMinute;
	}

	/** 업로드 URL 발급 요청 제한 */
	public void validateUploadRequest(UUID userId) {
		validate(uploadRequestsByUser, userId, uploadMaxRequestsPerMinute);
	}

	/** 파싱 시작 요청 제한 */
	public void validateParseRequest(UUID userId) {
		validate(parseRequestsByUser, userId, parseMaxRequestsPerMinute);
	}

	private void validate(Map<String, Deque<Long>> requestsByUser, UUID userId, int limit) {
		String key = userId.toString();
		Deque<Long> requestTimes = requestsByUser.computeIfAbsent(key, ignored -> new ArrayDeque<>());
		long now = System.currentTimeMillis();
		long windowStart = now - WINDOW.toMillis();

		// 사용자별 요청 처리 중에 새로운 요청이 올 것을 대비하여 동기화가 필요했습니다.
		// 큐 단위로 동기화를 수행하여, 사용자별 동기화를 도모했습니다.
		synchronized (requestTimes) {
			while (!requestTimes.isEmpty() && requestTimes.peekFirst() <= windowStart) {
				requestTimes.pollFirst();
			}

			if (requestTimes.size() >= limit) {
				long oldest = requestTimes.peekFirst();
				long retryAfterMillis = (oldest + WINDOW.toMillis()) - now;
				long retryAfterSeconds =
						Math.max(MIN_RETRY_AFTER_SECONDS, (long) Math.ceil(retryAfterMillis / 1000.0));
				throw new RateLimitExceededException(
						ErrorCode.TEMP_EXPENSE_RATE_LIMIT_EXCEEDED, retryAfterSeconds);
			}

			requestTimes.addLast(now);
		}
	}
}
