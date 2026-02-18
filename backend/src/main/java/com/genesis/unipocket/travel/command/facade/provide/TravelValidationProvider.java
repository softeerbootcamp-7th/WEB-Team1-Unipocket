package com.genesis.unipocket.travel.command.facade.provide;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.command.persistence.repository.TravelCommandRepository;
import com.genesis.unipocket.widget.common.validate.UserTravelValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TravelValidationProvider implements UserTravelValidator {

	private final TravelCommandRepository travelCommandRepository;

	@Override
	public void validateTravelInAccountBook(Long accountBookId, Long travelId) {
		boolean exists =
				travelCommandRepository.existsByIdAndAccountBookId(travelId, accountBookId);
		if (!exists) {
			throw new BusinessException(ErrorCode.TRAVEL_NOT_FOUND);
		}
	}
}
