package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TempExpenseAmountInfoTest {

	@Nested
	class DbPrecisionOverflowGuard {

		@Test
		void baseCurrencyAmount_exceeding_db_precision_is_set_to_null() {
			TempExpenseAmountInfo info =
					TempExpenseAmountInfo.of(
							CurrencyCode.JPY,
							new BigDecimal("50000"),
							CurrencyCode.KRW,
							new BigDecimal("419000000"),
							new BigDecimal("8.38"));

			assertThat(info.getBaseCurrencyAmount()).isNull();
		}

		@Test
		void localCurrencyAmount_exceeding_db_precision_is_set_to_null() {
			TempExpenseAmountInfo info =
					TempExpenseAmountInfo.of(
							CurrencyCode.JPY,
							new BigDecimal("100000000"),
							CurrencyCode.KRW,
							new BigDecimal("5000"),
							new BigDecimal("8.38"));

			assertThat(info.getLocalCurrencyAmount()).isNull();
		}

		@Test
		void recalculateBaseIfPossible_sets_null_when_result_exceeds_precision() {
			TempExpenseAmountInfo info =
					TempExpenseAmountInfo.of(
							CurrencyCode.USD,
							new BigDecimal("50000000"),
							CurrencyCode.KRW,
							null,
							new BigDecimal("1400"));

			TempExpenseAmountInfo recalculated = info.recalculateBaseIfPossible();

			assertThat(recalculated.getBaseCurrencyAmount()).isNull();
		}

		@Test
		void amounts_within_db_precision_are_kept_as_is() {
			BigDecimal local = new BigDecimal("99999999.99");
			BigDecimal base = new BigDecimal("50000.00");

			TempExpenseAmountInfo info =
					TempExpenseAmountInfo.of(
							CurrencyCode.USD,
							local,
							CurrencyCode.KRW,
							base,
							new BigDecimal("1400"));

			assertThat(info.getLocalCurrencyAmount()).isEqualByComparingTo(local);
			assertThat(info.getBaseCurrencyAmount()).isEqualByComparingTo(base);
		}
	}
}
