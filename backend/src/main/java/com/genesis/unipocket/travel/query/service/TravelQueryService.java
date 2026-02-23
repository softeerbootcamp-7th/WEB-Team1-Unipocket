package com.genesis.unipocket.travel.query.service;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import com.genesis.unipocket.travel.query.persistence.repository.TravelQueryRepository;
import com.genesis.unipocket.travel.query.persistence.response.TravelDetailQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.WidgetOrderDto;
import com.genesis.unipocket.travel.query.port.TravelImageAccessService;
import com.genesis.unipocket.travel.query.presentation.response.TravelListItemResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelQueryService {

	private final TravelQueryRepository travelQueryRepository;
	private final UserAccountBookValidator userAccountBookValidator;
	private final TravelImageAccessService travelImageAccessService;
	private final AccountBookQueryRepository accountBookQueryRepository;
	private final AnalysisBatchAggregationRepository analysisBatchAggregationRepository;

	@Value("${app.media.presigned-get-expiration-seconds:600}")
	private int presignedGetExpirationSeconds;

	public List<TravelListItemResponse> getTravels(Long accountBookId, UUID userId) {
		AccountBookDetailResponse accountBook =
				accountBookQueryRepository
						.findDetailById(userId, accountBookId)
						.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));

		List<TravelQueryResponse> travels =
				travelQueryRepository.findAllByAccountBookId(accountBookId);

		Map<Long, AnalysisBatchAggregationRepository.TravelAmountRow> amountByTravelId =
				analysisBatchAggregationRepository.aggregateAllTravelsRaw(accountBookId).stream()
						.collect(
								Collectors.toMap(
										AnalysisBatchAggregationRepository.TravelAmountRow
												::travelId,
										row -> row));

		return travels.stream()
				.map(
						t -> {
							var amounts =
									amountByTravelId.getOrDefault(
											t.travelId(),
											new AnalysisBatchAggregationRepository.TravelAmountRow(
													t.travelId(),
													BigDecimal.ZERO,
													BigDecimal.ZERO));
							return new TravelListItemResponse(
									t.travelId(),
									t.accountBookId(),
									t.travelPlaceName(),
									t.startDate(),
									t.endDate(),
									t.imageKey(),
									accountBook.localCountryCode(),
									amounts.totalLocalAmount(),
									accountBook.baseCountryCode(),
									amounts.totalBaseAmount());
						})
				.toList();
	}

	public TravelDetailQueryResponse getTravelDetail(
			Long accountBookId, Long travelId, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), accountBookId);

		TravelQueryResponse travel =
				travelQueryRepository
						.findById(travelId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));
		if (!travel.accountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TRAVEL_NOT_FOUND);
		}

		List<WidgetOrderDto> widgets = travelQueryRepository.findAllByTravelId(travelId);

		return TravelDetailQueryResponse.of(travel, widgets);
	}

	public String issueTravelImageViewUrl(Long accountBookId, UUID userId, String imageKey) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), accountBookId);
		if (imageKey == null || imageKey.isBlank()) {
			throw new BusinessException(ErrorCode.TRAVEL_INVALID_IMAGE_KEY);
		}

		if (!travelImageAccessService.isTravelImageKey(imageKey)) {
			throw new BusinessException(ErrorCode.TRAVEL_INVALID_IMAGE_KEY);
		}

		boolean existsInAccountBook =
				travelQueryRepository.existsByAccountBookIdAndImageKey(accountBookId, imageKey);
		if (!existsInAccountBook) {
			throw new BusinessException(ErrorCode.TRAVEL_IMAGE_NOT_FOUND);
		}
		if (!travelImageAccessService.exists(imageKey)) {
			throw new BusinessException(ErrorCode.TRAVEL_IMAGE_NOT_FOUND);
		}

		return travelImageAccessService.issueGetPath(
				imageKey, Duration.ofSeconds(presignedGetExpirationSeconds));
	}

	public int getTravelImageViewUrlExpirationSeconds() {
		return presignedGetExpirationSeconds;
	}
}
