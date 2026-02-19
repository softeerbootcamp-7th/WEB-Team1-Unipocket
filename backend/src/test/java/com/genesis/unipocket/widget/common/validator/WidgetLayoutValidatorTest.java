package com.genesis.unipocket.widget.common.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WidgetLayoutValidator 단위 테스트")
class WidgetLayoutValidatorTest {

	@Nested
	@DisplayName("기본값 적용")
	class DefaultValueTests {

		@Test
		@DisplayName("currencyType이 null이면 BASE로 적용된다")
		void validateAndNormalize_nullCurrencyType_appliesDefault() {
			List<WidgetItem> items =
					List.of(new WidgetItem(0, WidgetType.BUDGET, null, Period.MONTHLY));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result.get(0).currencyType()).isEqualTo(CurrencyType.BASE);
		}

		@Test
		@DisplayName("period가 null이면 ALL로 적용된다")
		void validateAndNormalize_nullPeriod_appliesDefault() {
			List<WidgetItem> items =
					List.of(new WidgetItem(0, WidgetType.BUDGET, CurrencyType.LOCAL, null));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result.get(0).period()).isEqualTo(Period.ALL);
		}

		@Test
		@DisplayName("currencyType과 period 모두 null이면 기본값이 적용된다")
		void validateAndNormalize_bothNull_appliesDefaults() {
			List<WidgetItem> items = List.of(new WidgetItem(0, WidgetType.BUDGET, null, null));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result.get(0).currencyType()).isEqualTo(CurrencyType.BASE);
			assertThat(result.get(0).period()).isEqualTo(Period.ALL);
		}

		@Test
		@DisplayName("값이 있으면 기본값으로 덮어쓰지 않는다")
		void validateAndNormalize_existingValues_preserved() {
			List<WidgetItem> items =
					List.of(new WidgetItem(0, WidgetType.BUDGET, CurrencyType.LOCAL, Period.DAILY));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result.get(0).currencyType()).isEqualTo(CurrencyType.LOCAL);
			assertThat(result.get(0).period()).isEqualTo(Period.DAILY);
		}
	}

	@Nested
	@DisplayName("order 유니크 검증")
	class OrderUniquenessTests {

		@Test
		@DisplayName("order가 중복되면 WIDGET_ORDER_DUPLICATED 예외가 발생한다")
		void validateAndNormalize_duplicateOrder_throwsException() {
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(0, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL));

			assertThatThrownBy(() -> WidgetLayoutValidator.validateAndNormalize(items))
					.isInstanceOf(BusinessException.class)
					.hasFieldOrPropertyWithValue("code", ErrorCode.WIDGET_ORDER_DUPLICATED);
		}

		@Test
		@DisplayName("order가 모두 다르면 검증을 통과한다")
		void validateAndNormalize_uniqueOrders_passes() {
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(1, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result).hasSize(2);
		}
	}

	@Nested
	@DisplayName("size 합 검증")
	class TotalSizeTests {

		@Test
		@DisplayName("size 합이 5를 초과하면 WIDGET_SIZE_EXCEEDED 예외가 발생한다")
		void validateAndNormalize_totalSizeExceeded_throwsException() {
			// CATEGORY(2) + BUDGET(1) + PERIOD(1) + COMPARISON(1) + PAYMENT(1) = 6 > 5
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.CATEGORY, CurrencyType.BASE, Period.ALL),
							new WidgetItem(1, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(2, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL),
							new WidgetItem(3, WidgetType.COMPARISON, CurrencyType.BASE, Period.ALL),
							new WidgetItem(4, WidgetType.PAYMENT, CurrencyType.BASE, Period.ALL));

			assertThatThrownBy(() -> WidgetLayoutValidator.validateAndNormalize(items))
					.isInstanceOf(BusinessException.class)
					.hasFieldOrPropertyWithValue("code", ErrorCode.WIDGET_SIZE_EXCEEDED);
		}

		@Test
		@DisplayName("size 합이 정확히 5이면 검증을 통과한다")
		void validateAndNormalize_totalSizeExactlyFive_passes() {
			// CATEGORY(2) + BUDGET(1) + PERIOD(1) + COMPARISON(1) = 5
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.CATEGORY, CurrencyType.BASE, Period.ALL),
							new WidgetItem(1, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(2, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL),
							new WidgetItem(
									3, WidgetType.COMPARISON, CurrencyType.BASE, Period.ALL));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result).hasSize(4);
		}

		@Test
		@DisplayName("size 합이 5 미만이면 검증을 통과한다")
		void validateAndNormalize_totalSizeBelowFive_passes() {
			// BUDGET(1) + PERIOD(1) = 2
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(1, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("빈 리스트는 검증을 통과한다")
		void validateAndNormalize_emptyList_passes() {
			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(List.of());

			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("정렬")
	class SortingTests {

		@Test
		@DisplayName("결과는 order ASC 기준으로 정렬된다")
		void validateAndNormalize_unsortedInput_returnsSorted() {
			List<WidgetItem> items =
					List.of(
							new WidgetItem(2, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL),
							new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(1, WidgetType.CATEGORY, CurrencyType.BASE, Period.ALL));

			List<WidgetItem> result = WidgetLayoutValidator.validateAndNormalize(items);

			assertThat(result.get(0).order()).isEqualTo(0);
			assertThat(result.get(1).order()).isEqualTo(1);
			assertThat(result.get(2).order()).isEqualTo(2);
		}
	}
}
