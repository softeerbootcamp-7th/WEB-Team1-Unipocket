package com.genesis.unipocket.expense.query.port;

import com.genesis.unipocket.expense.query.port.dto.ExpenseTravelResult;
import java.util.Collection;
import java.util.Map;

public interface TravelInfoReader {

	Map<Long, ExpenseTravelResult> readTravelInfoMap(
			Long accountBookId, Collection<Long> travelIds);
}
