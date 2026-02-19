package com.genesis.unipocket.expense.query.service;

import com.genesis.unipocket.expense.application.result.ExpenseTravelResult;
import java.util.Collection;
import java.util.Map;

public interface TravelInfoReader {

	Map<Long, ExpenseTravelResult> readTravelInfoMap(
			Long accountBookId, Collection<Long> travelIds);
}
