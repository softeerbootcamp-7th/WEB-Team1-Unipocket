package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TempExpensePaymentInfoTest {

	@Nested
	class Merge_CardLastFourDigits {

		private final TempExpensePaymentInfo existing =
				TempExpensePaymentInfo.of("1234", "APPROVAL-001");

		@Test
		void null_optional_keeps_existing_value() {
			// JSON에서 필드를 아예 보내지 않은 경우 → Optional 자체가 null → 기존값 유지
			TempExpensePaymentInfo merged = existing.merge(null, null);

			assertThat(merged.getCardLastFourDigits()).isEqualTo("1234");
			assertThat(merged.getApprovalNumber()).isEqualTo("APPROVAL-001");
		}

		@Test
		void empty_optional_clears_to_null() {
			// JSON에서 "cardLastFourDigits": null 로 보낸 경우 → Optional.empty() → null로 초기화
			TempExpensePaymentInfo merged = existing.merge(Optional.empty(), null);

			assertThat(merged.getCardLastFourDigits()).isNull();
			assertThat(merged.getApprovalNumber()).isEqualTo("APPROVAL-001");
		}

		@Test
		void present_optional_sets_new_value() {
			// JSON에서 "cardLastFourDigits": "5678" 로 보낸 경우 → Optional.of("5678") → 새 값 설정
			TempExpensePaymentInfo merged = existing.merge(Optional.of("5678"), null);

			assertThat(merged.getCardLastFourDigits()).isEqualTo("5678");
			assertThat(merged.getApprovalNumber()).isEqualTo("APPROVAL-001");
		}

		@Test
		void both_fields_updated_together() {
			TempExpensePaymentInfo merged = existing.merge(Optional.of("9999"), "APPROVAL-002");

			assertThat(merged.getCardLastFourDigits()).isEqualTo("9999");
			assertThat(merged.getApprovalNumber()).isEqualTo("APPROVAL-002");
		}

		@Test
		void clear_card_and_update_approval_together() {
			TempExpensePaymentInfo merged = existing.merge(Optional.empty(), "APPROVAL-NEW");

			assertThat(merged.getCardLastFourDigits()).isNull();
			assertThat(merged.getApprovalNumber()).isEqualTo("APPROVAL-NEW");
		}

		@Test
		void merge_on_empty_info_sets_values() {
			TempExpensePaymentInfo empty = TempExpensePaymentInfo.empty();

			TempExpensePaymentInfo merged = empty.merge(Optional.of("1234"), "APPROVAL-001");

			assertThat(merged.getCardLastFourDigits()).isEqualTo("1234");
			assertThat(merged.getApprovalNumber()).isEqualTo("APPROVAL-001");
		}

		@Test
		void merge_on_empty_info_with_absent_fields_stays_null() {
			TempExpensePaymentInfo empty = TempExpensePaymentInfo.empty();

			TempExpensePaymentInfo merged = empty.merge(null, null);

			assertThat(merged.getCardLastFourDigits()).isNull();
			assertThat(merged.getApprovalNumber()).isNull();
		}
	}
}
