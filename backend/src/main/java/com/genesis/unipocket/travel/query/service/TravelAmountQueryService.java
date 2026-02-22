package com.genesis.unipocket.travel.query.service;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.query.persistence.repository.TravelQueryRepository;
import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import com.genesis.unipocket.travel.query.presentation.response.TravelAmountResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelAmountQueryService {

	private final AccountBookQueryRepository accountBookQueryRepository;
	private final TravelQueryRepository travelQueryRepository;
	private final AnalysisBatchAggregationRepository analysisBatchAggregationRepository;
	private final ExchangeRateService exchangeRateService;

	public TravelAmountResponse getTravelAmount(Long accountBookId, Long travelId, String userId) {
		AccountBookDetailResponse accountBook = getAccessibleAccountBook(accountBookId, userId);
		TravelQueryResponse travel = getAccessibleTravel(accountBookId, travelId);

		CurrencyCode accountBookLocalCurrency = accountBook.localCountryCode().getCurrencyCode();
		var raw = analysisBatchAggregationRepository.aggregateTravelRaw(accountBookId, travelId);
		BigDecimal correctedLocal =
				computeCorrectedLocalAmount(
						analysisBatchAggregationRepository
								.aggregateTravelLocalAmountGroupedByCurrency(
										accountBookId, travelId),
						accountBookLocalCurrency,
						travel.startDate().atStartOfDay().atOffset(ZoneOffset.UTC));

		return new TravelAmountResponse(
				accountBook.localCountryCode(),
				accountBook.localCountryCode().getCurrencyCode(),
				accountBook.baseCountryCode(),
				accountBook.baseCountryCode().getCurrencyCode(),
				correctedLocal,
				raw.totalBaseAmount());
	}

	private AccountBookDetailResponse getAccessibleAccountBook(Long accountBookId, String userId) {
		UUID userUuid = UUID.fromString(userId);
		return accountBookQueryRepository
				.findDetailById(userUuid, accountBookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
	}

	private TravelQueryResponse getAccessibleTravel(Long accountBookId, Long travelId) {
		TravelQueryResponse travel =
				travelQueryRepository
						.findById(travelId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));
		if (!travel.accountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TRAVEL_NOT_FOUND);
		}
		return travel;
	}

	private BigDecimal computeCorrectedLocalAmount(
			List<AnalysisBatchAggregationRepository.LocalCurrencyGroupRow> groups,
			CurrencyCode targetCurrency,
			OffsetDateTime refDateTime) {
		BigDecimal total = BigDecimal.ZERO;
		for (var group : groups) {
			if (group.localCurrencyCode() == null) {
				continue;
			}
			CurrencyCode from = parseCurrencyCode(group.localCurrencyCode());
			if (from == null) {
				continue;
			}
			BigDecimal amount = group.localAmountSum();
			if (from == targetCurrency) {
				total = total.add(amount);
			} else {
				total =
						total.add(
								exchangeRateService.convertAmount(
										amount, from, targetCurrency, refDateTime));
			}
		}
		return total;
	}

	private CurrencyCode parseCurrencyCode(String rawCode) {
		try {
			int ordinal = Integer.parseInt(rawCode);
			CurrencyCode[] values = CurrencyCode.values();
			return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
		} catch (NumberFormatException ignored) {
			try {
				return CurrencyCode.valueOf(rawCode);
			} catch (IllegalArgumentException ignored2) {
				return null;
			}
		}
	}
}
