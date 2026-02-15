package com.genesis.unipocket.travel.query.service;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import com.genesis.unipocket.travel.query.persistence.repository.TravelQueryRepository;
import com.genesis.unipocket.travel.query.persistence.response.TravelDetailQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.WidgetOrderDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelQueryService {

	private final TravelQueryRepository travelQueryRepository;
	private final UserAccountBookValidator userAccountBookValidator;

	public List<TravelQueryResponse> getTravels(Long accountBookId, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), accountBookId);
		return travelQueryRepository.findAllByAccountBookId(accountBookId);
	}

	public TravelDetailQueryResponse getTravelDetail(
			Long accountBookId, Long travelId, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), accountBookId);

		TravelQueryResponse travel =
				travelQueryRepository
						.findById(travelId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		List<WidgetOrderDto> widgets = travelQueryRepository.findAllByTravelId(travelId);

		return TravelDetailQueryResponse.of(travel, widgets);
	}
}
