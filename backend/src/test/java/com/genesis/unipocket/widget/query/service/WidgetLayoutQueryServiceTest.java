package com.genesis.unipocket.widget.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import com.genesis.unipocket.widget.query.persistence.repository.AccountBookWidgetQueryRepository;
import com.genesis.unipocket.widget.query.persistence.repository.TravelWidgetQueryRepository;
import com.genesis.unipocket.widget.query.persistence.response.WidgetItemQueryRes;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WidgetLayoutQueryService 단위 테스트")
class WidgetLayoutQueryServiceTest {

	@Mock private AccountBookWidgetQueryRepository accountBookWidgetQueryRepository;
	@Mock private TravelWidgetQueryRepository travelWidgetQueryRepository;
	@InjectMocks private WidgetLayoutQueryService widgetLayoutQueryService;

	@Test
	@DisplayName("AccountBook 위젯 조회 시 repository에 위임한다")
	void getAccountBookWidgets_delegatesToRepository() {
		// given
		Long accountBookId = 1L;
		List<WidgetItemQueryRes> expected =
				List.of(
						new WidgetItemQueryRes(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
						new WidgetItemQueryRes(
								1, WidgetType.CATEGORY, CurrencyType.LOCAL, Period.MONTHLY));

		when(accountBookWidgetQueryRepository.findAllByAccountBookId(accountBookId))
				.thenReturn(expected);

		// when
		List<WidgetItemQueryRes> result =
				widgetLayoutQueryService.getAccountBookWidgets(accountBookId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).widgetType()).isEqualTo(WidgetType.BUDGET);
		assertThat(result.get(1).widgetType()).isEqualTo(WidgetType.CATEGORY);
		verify(accountBookWidgetQueryRepository).findAllByAccountBookId(accountBookId);
	}

	@Test
	@DisplayName("Travel 위젯 조회 시 repository에 위임한다")
	void getTravelWidgets_delegatesToRepository() {
		// given
		Long travelId = 10L;
		List<WidgetItemQueryRes> expected =
				List.of(
						new WidgetItemQueryRes(
								0, WidgetType.CURRENCY, CurrencyType.LOCAL, Period.DAILY));

		when(travelWidgetQueryRepository.findAllByTravelId(travelId)).thenReturn(expected);

		// when
		List<WidgetItemQueryRes> result = widgetLayoutQueryService.getTravelWidgets(travelId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).widgetType()).isEqualTo(WidgetType.CURRENCY);
		verify(travelWidgetQueryRepository).findAllByTravelId(travelId);
	}

	@Test
	@DisplayName("위젯이 없으면 빈 리스트를 반환한다")
	void getAccountBookWidgets_noWidgets_returnsEmptyList() {
		// given
		Long accountBookId = 999L;
		when(accountBookWidgetQueryRepository.findAllByAccountBookId(accountBookId))
				.thenReturn(List.of());

		// when
		List<WidgetItemQueryRes> result =
				widgetLayoutQueryService.getAccountBookWidgets(accountBookId);

		// then
		assertThat(result).isEmpty();
	}
}
