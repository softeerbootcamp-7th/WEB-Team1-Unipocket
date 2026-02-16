package com.genesis.unipocket.tempexpense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.exception.RateLimitExceededException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TemporaryExpenseRateLimitServiceTest {

	@Test
	@DisplayName("업로드 요청이 분당 제한을 초과하면 429 예외를 던진다")
	void validateUploadRequest_초과시_예외() {
		TemporaryExpenseRateLimitService service = new TemporaryExpenseRateLimitService(1, 5);
		UUID userId = UUID.randomUUID();

		service.validateUploadRequest(userId);

		assertThatThrownBy(() -> service.validateUploadRequest(userId))
				.isInstanceOf(RateLimitExceededException.class)
				.satisfies(
						ex ->
								assertThat(((RateLimitExceededException) ex).getCode())
										.isEqualTo(ErrorCode.TEMP_EXPENSE_RATE_LIMIT_EXCEEDED))
				.satisfies(
						ex ->
								assertThat(((RateLimitExceededException) ex).getRetryAfterSeconds())
										.isGreaterThanOrEqualTo(1L));
	}

	@Test
	@DisplayName("업로드/파싱 제한 카운터는 서로 독립적이다")
	void uploadAndParseBucket_독립() {
		TemporaryExpenseRateLimitService service = new TemporaryExpenseRateLimitService(1, 1);
		UUID userId = UUID.randomUUID();

		service.validateUploadRequest(userId);
		service.validateParseRequest(userId);

		assertThatThrownBy(() -> service.validateUploadRequest(userId))
				.isInstanceOf(RateLimitExceededException.class);
		assertThatThrownBy(() -> service.validateParseRequest(userId))
				.isInstanceOf(RateLimitExceededException.class);
	}
}
