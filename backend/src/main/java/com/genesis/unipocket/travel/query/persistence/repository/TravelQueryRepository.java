package com.genesis.unipocket.travel.query.persistence.repository;

import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.WidgetOrderDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TravelQueryRepository {

	@PersistenceContext private EntityManager em;

	public Optional<TravelQueryResponse> findById(Long id) {
		List<TravelQueryResponse> result =
				em.createQuery(
								"SELECT new"
									+ " com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse(t.id,"
									+ " t.accountBookId, t.travelPlaceName, t.startDate, t.endDate,"
									+ " t.imageKey) FROM Travel t WHERE t.id = :id",
								TravelQueryResponse.class)
						.setParameter("id", id)
						.getResultList();
		return result.stream().findFirst();
	}

	public List<TravelQueryResponse> findAllByAccountBookId(Long accountBookId) {
		return em.createQuery(
						"SELECT new"
							+ " com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse(t.id,"
							+ " t.accountBookId, t.travelPlaceName, t.startDate, t.endDate,"
							+ " t.imageKey) FROM Travel t WHERE t.accountBookId = :accountBookId",
						TravelQueryResponse.class)
				.setParameter("accountBookId", accountBookId)
				.getResultList();
	}

	public List<WidgetOrderDto> findAllByTravelId(Long travelId) {
		return em.createQuery(
						"SELECT new"
							+ " com.genesis.unipocket.travel.query.persistence.response.WidgetQueryResponse(w.widgetType,"
							+ " w.widgetOrder) FROM TravelWidget w WHERE w.travel.id = :travelId"
							+ " ORDER BY w.widgetOrder ASC",
						WidgetOrderDto.class)
				.setParameter("travelId", travelId)
				.getResultList();
	}
}
