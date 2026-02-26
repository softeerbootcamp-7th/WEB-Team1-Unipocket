package com.genesis.unipocket.tempexpense.command.presentation.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TemporaryExpenseMetaBulkUpdateItemRequestDeserializationTest {

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

	@Test
	void cardAbsent_shouldBeNull_notOptionalEmpty() throws Exception {
		// 카드 필드가 아예 없는 경우 → null이어야 "기존값 유지"
		String json = "{\"tempExpenseId\": 1, \"localCurrencyAmount\": 50.00}";

		TemporaryExpenseMetaBulkUpdateItemRequest req =
				mapper.readValue(json, TemporaryExpenseMetaBulkUpdateItemRequest.class);

		// null이면 기존값 유지, Optional.empty()면 카드 초기화 (버그!)
		assertThat(req.getCardLastFourDigits()).as("카드 필드 미전송 시 null이어야 함 (기존값 유지 시맨틱)").isNull();
	}

	@Test
	void cardExplicitNull_shouldBeOptionalEmpty() throws Exception {
		// "cardLastFourDigits": null → Optional.empty() (카드 초기화)
		String json = "{\"tempExpenseId\": 1, \"cardLastFourDigits\": null}";

		TemporaryExpenseMetaBulkUpdateItemRequest req =
				mapper.readValue(json, TemporaryExpenseMetaBulkUpdateItemRequest.class);

		assertThat(req.getCardLastFourDigits())
				.as("명시적 null 전송 시 Optional.empty()여야 함 (카드 초기화 시맨틱)")
				.isEqualTo(Optional.empty());
	}

	@Test
	void cardWithValue_shouldBeOptionalOf() throws Exception {
		// "cardLastFourDigits": "1400" → Optional.of("1400") (카드 변경)
		String json = "{\"tempExpenseId\": 1, \"cardLastFourDigits\": \"1400\"}";

		TemporaryExpenseMetaBulkUpdateItemRequest req =
				mapper.readValue(json, TemporaryExpenseMetaBulkUpdateItemRequest.class);

		assertThat(req.getCardLastFourDigits())
				.as("카드 값 전송 시 Optional.of(값)이어야 함 (카드 변경 시맨틱)")
				.isEqualTo(Optional.of("1400"));
	}
}
