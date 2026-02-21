package com.genesis.unipocket.travel.query.provide;

import com.genesis.unipocket.expense.query.port.TravelInfoReader;
import com.genesis.unipocket.expense.query.port.dto.ExpenseTravelResult;
import com.genesis.unipocket.travel.query.persistence.repository.TravelQueryRepository;
import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseTravelInfoReader implements TravelInfoReader {

	private final TravelQueryRepository travelQueryRepository;

	@Override
	public Map<Long, ExpenseTravelResult> readTravelInfoMap(
			Long accountBookId, Collection<Long> travelIds) {
		if (travelIds == null || travelIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<Long> distinctIds = travelIds.stream().filter(id -> id != null).distinct().toList();
		if (distinctIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<TravelQueryResponse> travels =
				travelQueryRepository.findAllByIdsAndAccountBookId(distinctIds, accountBookId);

		return travels.stream()
				.collect(
						Collectors.toMap(
								TravelQueryResponse::travelId,
								travel ->
										new ExpenseTravelResult(
												travel.travelId(),
												travel.travelPlaceName(),
												travel.imageKey()),
								(existing, replacement) -> existing));
	}
}
