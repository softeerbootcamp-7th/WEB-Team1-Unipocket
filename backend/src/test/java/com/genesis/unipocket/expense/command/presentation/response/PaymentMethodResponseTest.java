package com.genesis.unipocket.expense.command.presentation.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import org.junit.jupiter.api.Test;

class PaymentMethodResponseTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void userCardId가_null이면_cash_응답이다() {
		PaymentMethodResponse response = PaymentMethodResponse.from(null, null, null, null);

		assertThat(response.isCash()).isTrue();
		assertThat(response.card()).isNull();
	}

	@Test
	void 카드결제일때_cardCompany는_인덱스_숫자로_직렬화된다() throws Exception {
		PaymentMethodResponse response =
				PaymentMethodResponse.from(1L, CardCompany.HYUNDAI, "현대카드", "1234");

		String json = objectMapper.writeValueAsString(response);

		assertThat(objectMapper.readTree(json).get("isCash").asBoolean()).isFalse();
		assertThat(objectMapper.readTree(json).get("card").get("company").asInt())
				.isEqualTo(CardCompany.HYUNDAI.ordinal());
	}
}
