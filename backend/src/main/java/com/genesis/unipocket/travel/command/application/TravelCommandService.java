package com.genesis.unipocket.travel.command.application;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.media.command.application.MediaPathPrefixManager;
import com.genesis.unipocket.travel.command.application.command.CreateTravelCommand;
import com.genesis.unipocket.travel.command.application.command.PatchTravelCommand;
import com.genesis.unipocket.travel.command.application.command.UpdateTravelCommand;
import com.genesis.unipocket.travel.command.application.result.CreateTravelResult;
import com.genesis.unipocket.travel.command.application.result.TravelBudgetUpdateResult;
import com.genesis.unipocket.travel.command.persistence.entity.Travel;
import com.genesis.unipocket.travel.command.persistence.repository.TravelCommandRepository;
import com.genesis.unipocket.widget.command.persistence.repository.TravelWidgetJpaRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelCommandService {

	private static final int MAX_TRAVEL_COUNT_BY_ACCOUNT_BOOK = 10;
	private final TravelCommandRepository travelRepository;
	private final TravelWidgetJpaRepository travelWidgetJpaRepository;
	private final MediaObjectStorage mediaObjectStorage;
	private final MediaPathPrefixManager mediaPathPrefixManager;

	@Transactional
	public CreateTravelResult createTravel(CreateTravelCommand command) {
		validateImageKey(command.imageKey());

		int count = travelRepository.countByAccountBookId(command.accountBookId());

		if (count >= MAX_TRAVEL_COUNT_BY_ACCOUNT_BOOK) {
			throw new BusinessException(ErrorCode.TRAVEL_COUNT_EXCEEDED);
		}

		Travel travel =
				Travel.builder()
						.accountBookId(command.accountBookId())
						.travelPlaceName(command.travelPlaceName())
						.startDate(command.startDate())
						.endDate(command.endDate())
						.imageKey(command.imageKey())
						.build();

		travel.validateDateRange();

		Long travelId = travelRepository.save(travel).getId();
		return new CreateTravelResult(travelId);
	}

	public Travel getTravel(Long travelId) {
		return travelRepository
				.findById(travelId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));
	}

	@Transactional
	public void updateTravel(Long accountBookId, UpdateTravelCommand command) {
		Travel travel =
				travelRepository
						.findById(command.travelId())
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		if (!travel.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TRAVEL_NOT_IN_ACCOUNT_BOOK);
		}

		validateImageKey(command.imageKey());

		travel.update(
				command.travelPlaceName(),
				command.startDate(),
				command.endDate(),
				command.imageKey());
	}

	@Transactional
	public void deleteTravel(Long travelId) {
		Travel travel =
				travelRepository
						.findById(travelId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		travelWidgetJpaRepository.deleteAllByTravelId(travelId);
		travelRepository.delete(travel);
	}

	@Transactional
	public void patchTravel(PatchTravelCommand command) {
		Travel travel =
				travelRepository
						.findById(command.travelId())
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		if (command.travelPlaceName() != null) {
			travel.updateName(command.travelPlaceName());
		}
		if (command.imageKey() != null) {
			validateImageKey(command.imageKey());
			travel.updateImage(command.imageKey());
		}
		if (command.startDate() != null && command.endDate() != null) {
			travel.updatePeriod(command.startDate(), command.endDate());
		} else if (command.startDate() != null) {
			travel.updatePeriod(command.startDate(), travel.getEndDate());
		} else if (command.endDate() != null) {
			travel.updatePeriod(travel.getStartDate(), command.endDate());
		}
	}

	@Transactional
	public TravelBudgetUpdateResult updateTravelBudget(
			Long accountBookId, Long travelId, BigDecimal budget) {
		Travel travel =
				travelRepository
						.findById(travelId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		if (!travel.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TRAVEL_NOT_IN_ACCOUNT_BOOK);
		}

		travel.updateBudget(budget);
		return new TravelBudgetUpdateResult(
				travel.getId(), travel.getBudget(), travel.getBudgetCreatedAt());
	}

	private void validateImageKey(String imageKey) {
		if (imageKey == null || imageKey.isBlank()) {
			return;
		}

		if (!mediaPathPrefixManager.isTravelImageKey(imageKey)) {
			throw new BusinessException(ErrorCode.TRAVEL_INVALID_IMAGE_KEY);
		}
		if (!mediaObjectStorage.exists(imageKey)) {
			throw new BusinessException(ErrorCode.TRAVEL_IMAGE_NOT_FOUND);
		}
	}
}
