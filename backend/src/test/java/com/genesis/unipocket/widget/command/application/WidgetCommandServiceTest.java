package com.genesis.unipocket.widget.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.widget.command.application.command.UpdateAccountBookWidgetsCommand;
import com.genesis.unipocket.widget.command.application.command.UpdateTravelWidgetsCommand;
import com.genesis.unipocket.widget.command.application.result.UpdateAccountBookWidgetsResult;
import com.genesis.unipocket.widget.command.application.result.UpdateTravelWidgetsResult;
import com.genesis.unipocket.widget.command.persistence.repository.AccountBookWidgetJpaRepository;
import com.genesis.unipocket.widget.command.persistence.repository.TravelWidgetJpaRepository;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WidgetCommandService 단위 테스트")
class WidgetCommandServiceTest {

	@Mock private AccountBookWidgetJpaRepository accountBookWidgetJpaRepository;
	@Mock private TravelWidgetJpaRepository travelWidgetJpaRepository;
	@InjectMocks private WidgetCommandService widgetCommandService;

	@Nested
	@DisplayName("AccountBook 위젯 순서 수정")
	class UpdateAccountBookWidgetsTests {

		@Test
		@DisplayName("정상 요청 시 기존 데이터 삭제 후 새로 저장하고 결과를 반환한다")
		void updateAccountBookWidgets_success() {
			// given
			Long accountBookId = 1L;
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(
									1, WidgetType.CATEGORY, CurrencyType.LOCAL, Period.MONTHLY));

			UpdateAccountBookWidgetsCommand command =
					new UpdateAccountBookWidgetsCommand(accountBookId, items);

			when(accountBookWidgetJpaRepository.saveAll(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			UpdateAccountBookWidgetsResult result =
					widgetCommandService.updateAccountBookWidgets(command);

			// then
			assertThat(result.accountBookId()).isEqualTo(accountBookId);
			assertThat(result.items()).hasSize(2);
			assertThat(result.items().get(0).widgetType()).isEqualTo(WidgetType.BUDGET);
			assertThat(result.items().get(1).widgetType()).isEqualTo(WidgetType.CATEGORY);
		}

		@Test
		@DisplayName("삭제 → flush → 저장 순서로 실행된다")
		void updateAccountBookWidgets_executionOrder() {
			// given
			Long accountBookId = 1L;
			List<WidgetItem> items =
					List.of(new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL));

			UpdateAccountBookWidgetsCommand command =
					new UpdateAccountBookWidgetsCommand(accountBookId, items);

			when(accountBookWidgetJpaRepository.saveAll(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			widgetCommandService.updateAccountBookWidgets(command);

			// then
			InOrder inOrder = Mockito.inOrder(accountBookWidgetJpaRepository);
			inOrder.verify(accountBookWidgetJpaRepository).deleteAllByAccountBookId(accountBookId);
			inOrder.verify(accountBookWidgetJpaRepository).flush();
			inOrder.verify(accountBookWidgetJpaRepository).saveAll(any());
		}

		@Test
		@DisplayName("order 중복 시 WIDGET_ORDER_DUPLICATED 예외가 발생한다")
		void updateAccountBookWidgets_duplicateOrder_throwsException() {
			// given
			Long accountBookId = 1L;
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(0, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL));

			UpdateAccountBookWidgetsCommand command =
					new UpdateAccountBookWidgetsCommand(accountBookId, items);

			// when & then
			assertThatThrownBy(() -> widgetCommandService.updateAccountBookWidgets(command))
					.isInstanceOf(BusinessException.class)
					.hasFieldOrPropertyWithValue("code", ErrorCode.WIDGET_ORDER_DUPLICATED);
		}

		@Test
		@DisplayName("size 합이 5를 초과하면 WIDGET_SIZE_EXCEEDED 예외가 발생한다")
		void updateAccountBookWidgets_sizeExceeded_throwsException() {
			// given
			Long accountBookId = 1L;
			List<WidgetItem> items =
					List.of(
							new WidgetItem(0, WidgetType.CATEGORY, CurrencyType.BASE, Period.ALL),
							new WidgetItem(1, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
							new WidgetItem(2, WidgetType.PERIOD, CurrencyType.BASE, Period.ALL),
							new WidgetItem(3, WidgetType.COMPARISON, CurrencyType.BASE, Period.ALL),
							new WidgetItem(4, WidgetType.PAYMENT, CurrencyType.BASE, Period.ALL));

			UpdateAccountBookWidgetsCommand command =
					new UpdateAccountBookWidgetsCommand(accountBookId, items);

			// when & then
			assertThatThrownBy(() -> widgetCommandService.updateAccountBookWidgets(command))
					.isInstanceOf(BusinessException.class)
					.hasFieldOrPropertyWithValue("code", ErrorCode.WIDGET_SIZE_EXCEEDED);
		}

		@Test
		@DisplayName("null 기본값이 적용된 결과가 반환된다")
		void updateAccountBookWidgets_nullDefaults_applied() {
			// given
			Long accountBookId = 1L;
			List<WidgetItem> items = List.of(new WidgetItem(0, WidgetType.BUDGET, null, null));

			UpdateAccountBookWidgetsCommand command =
					new UpdateAccountBookWidgetsCommand(accountBookId, items);

			when(accountBookWidgetJpaRepository.saveAll(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			UpdateAccountBookWidgetsResult result =
					widgetCommandService.updateAccountBookWidgets(command);

			// then
			assertThat(result.items().get(0).currencyType()).isEqualTo(CurrencyType.BASE);
			assertThat(result.items().get(0).period()).isEqualTo(Period.ALL);
		}
	}

	@Nested
	@DisplayName("Travel 위젯 순서 수정")
	class UpdateTravelWidgetsTests {

		@Test
		@DisplayName("정상 요청 시 기존 데이터 삭제 후 새로 저장하고 결과를 반환한다")
		void updateTravelWidgets_success() {
			// given
			Long travelId = 10L;
			List<WidgetItem> items =
					List.of(
							new WidgetItem(
									0, WidgetType.CURRENCY, CurrencyType.LOCAL, Period.DAILY),
							new WidgetItem(1, WidgetType.PAYMENT, CurrencyType.BASE, Period.ALL));

			UpdateTravelWidgetsCommand command = new UpdateTravelWidgetsCommand(travelId, items);

			when(travelWidgetJpaRepository.saveAll(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			UpdateTravelWidgetsResult result = widgetCommandService.updateTravelWidgets(command);

			// then
			assertThat(result.travelId()).isEqualTo(travelId);
			assertThat(result.items()).hasSize(2);
		}

		@Test
		@DisplayName("삭제 → flush → 저장 순서로 실행된다")
		void updateTravelWidgets_executionOrder() {
			// given
			Long travelId = 10L;
			List<WidgetItem> items =
					List.of(new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL));

			UpdateTravelWidgetsCommand command = new UpdateTravelWidgetsCommand(travelId, items);

			when(travelWidgetJpaRepository.saveAll(any()))
					.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			widgetCommandService.updateTravelWidgets(command);

			// then
			InOrder inOrder = Mockito.inOrder(travelWidgetJpaRepository);
			inOrder.verify(travelWidgetJpaRepository).deleteAllByTravelId(travelId);
			inOrder.verify(travelWidgetJpaRepository).flush();
			inOrder.verify(travelWidgetJpaRepository).saveAll(any());
		}
	}
}
