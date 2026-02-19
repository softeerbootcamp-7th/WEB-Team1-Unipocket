package com.genesis.unipocket.widget.command.application;

import com.genesis.unipocket.widget.command.application.command.UpdateAccountBookWidgetsCommand;
import com.genesis.unipocket.widget.command.application.command.UpdateTravelWidgetsCommand;
import com.genesis.unipocket.widget.command.application.result.UpdateAccountBookWidgetsResult;
import com.genesis.unipocket.widget.command.application.result.UpdateTravelWidgetsResult;
import com.genesis.unipocket.widget.command.persistence.converter.AccountBookWidgetCommandConverter;
import com.genesis.unipocket.widget.command.persistence.converter.TravelWidgetCommandConverter;
import com.genesis.unipocket.widget.command.persistence.entity.AccountBookWidgetEntity;
import com.genesis.unipocket.widget.command.persistence.entity.TravelWidgetEntity;
import com.genesis.unipocket.widget.command.persistence.repository.AccountBookWidgetJpaRepository;
import com.genesis.unipocket.widget.command.persistence.repository.TravelWidgetJpaRepository;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.validator.WidgetLayoutValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetCommandService {

	private final AccountBookWidgetJpaRepository accountBookWidgetJpaRepository;
	private final TravelWidgetJpaRepository travelWidgetJpaRepository;

	@Transactional
	public UpdateAccountBookWidgetsResult updateAccountBookWidgets(
			UpdateAccountBookWidgetsCommand command) {
		List<WidgetItem> validated = WidgetLayoutValidator.validateAndNormalize(command.items());

		accountBookWidgetJpaRepository.deleteAllByAccountBookId(command.accountBookId());
		accountBookWidgetJpaRepository.flush();

		List<AccountBookWidgetEntity> entities =
				AccountBookWidgetCommandConverter.toEntities(command.accountBookId(), validated);
		List<AccountBookWidgetEntity> saved = accountBookWidgetJpaRepository.saveAll(entities);

		List<WidgetItem> resultItems = AccountBookWidgetCommandConverter.toWidgetItems(saved);
		return new UpdateAccountBookWidgetsResult(command.accountBookId(), resultItems);
	}

	@Transactional
	public UpdateTravelWidgetsResult updateTravelWidgets(UpdateTravelWidgetsCommand command) {
		List<WidgetItem> validated = WidgetLayoutValidator.validateAndNormalize(command.items());

		travelWidgetJpaRepository.deleteAllByTravelId(command.travelId());
		travelWidgetJpaRepository.flush();

		List<TravelWidgetEntity> entities =
				TravelWidgetCommandConverter.toEntities(command.travelId(), validated);
		List<TravelWidgetEntity> saved = travelWidgetJpaRepository.saveAll(entities);

		List<WidgetItem> resultItems = TravelWidgetCommandConverter.toWidgetItems(saved);
		return new UpdateTravelWidgetsResult(command.travelId(), resultItems);
	}
}
