package com.genesis.unipocket.widget.query.service;

import com.genesis.unipocket.widget.query.persistence.repository.AccountBookWidgetQueryRepository;
import com.genesis.unipocket.widget.query.persistence.repository.TravelWidgetQueryRepository;
import com.genesis.unipocket.widget.query.persistence.response.WidgetItemQueryRes;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetLayoutQueryService {

	private final AccountBookWidgetQueryRepository accountBookWidgetQueryRepository;
	private final TravelWidgetQueryRepository travelWidgetQueryRepository;

	public List<WidgetItemQueryRes> getAccountBookWidgets(Long accountBookId) {
		return accountBookWidgetQueryRepository.findAllByAccountBookId(accountBookId);
	}

	public List<WidgetItemQueryRes> getTravelWidgets(Long travelId) {
		return travelWidgetQueryRepository.findAllByTravelId(travelId);
	}
}
