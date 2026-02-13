package com.genesis.unipocket.widget.command.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.command.application.WidgetCommandService;
import com.genesis.unipocket.widget.command.application.result.UpdateAccountBookWidgetsResult;
import com.genesis.unipocket.widget.command.application.result.UpdateTravelWidgetsResult;
import com.genesis.unipocket.widget.command.facade.response.AccountBookWidgetsRes;
import com.genesis.unipocket.widget.command.facade.response.TravelWidgetsRes;
import com.genesis.unipocket.widget.command.presentation.request.WidgetItemRequest;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WidgetCommandOrchestrator 단위 테스트")
class WidgetCommandOrchestratorTest {

	@Mock private WidgetCommandService widgetCommandService;
	@InjectMocks private WidgetCommandOrchestrator orchestrator;

	@Test
	@DisplayName("AccountBook 위젯 수정 요청을 Command로 변환하여 서비스에 전달한다")
	void updateAccountBookWidgets_delegatesToService() {
		// given
		Long accountBookId = 1L;
		List<WidgetItemRequest> requests =
				List.of(new WidgetItemRequest(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL));

		List<WidgetItem> expectedItems =
				List.of(new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL));

		when(widgetCommandService.updateAccountBookWidgets(
						argThat(
								cmd ->
										cmd.accountBookId().equals(accountBookId)
												&& cmd.items().equals(expectedItems))))
				.thenReturn(new UpdateAccountBookWidgetsResult(accountBookId, expectedItems));

		// when
		AccountBookWidgetsRes result =
				orchestrator.updateAccountBookWidgets(accountBookId, requests);

		// then
		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).widgetType()).isEqualTo(WidgetType.BUDGET);
		verify(widgetCommandService)
				.updateAccountBookWidgets(
						argThat(cmd -> cmd.accountBookId().equals(accountBookId)));
	}

	@Test
	@DisplayName("Travel 위젯 수정 요청을 Command로 변환하여 서비스에 전달한다")
	void updateTravelWidgets_delegatesToService() {
		// given
		Long travelId = 10L;
		List<WidgetItemRequest> requests =
				List.of(
						new WidgetItemRequest(
								0, WidgetType.CURRENCY, CurrencyType.LOCAL, Period.DAILY));

		List<WidgetItem> expectedItems =
				List.of(new WidgetItem(0, WidgetType.CURRENCY, CurrencyType.LOCAL, Period.DAILY));

		when(widgetCommandService.updateTravelWidgets(
						argThat(
								cmd ->
										cmd.travelId().equals(travelId)
												&& cmd.items().equals(expectedItems))))
				.thenReturn(new UpdateTravelWidgetsResult(travelId, expectedItems));

		// when
		TravelWidgetsRes result = orchestrator.updateTravelWidgets(travelId, requests);

		// then
		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).widgetType()).isEqualTo(WidgetType.CURRENCY);
		verify(widgetCommandService)
				.updateTravelWidgets(argThat(cmd -> cmd.travelId().equals(travelId)));
	}
}
