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

	public List<TravelQueryResponse> findAllByIdsAndAccountBookId(
			List<Long> travelIds, Long accountBookId) {
		if (travelIds == null || travelIds.isEmpty()) {
			return List.of();
		}

		return em.createQuery(
						"SELECT new"
							+ " com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse(t.id,"
							+ " t.accountBookId, t.travelPlaceName, t.startDate, t.endDate,"
							+ " t.imageKey) FROM Travel t WHERE t.id IN :travelIds AND t.accountBookId ="
							+ " :accountBookId",
						TravelQueryResponse.class)
				.setParameter("travelIds", travelIds)
				.setParameter("accountBookId", accountBookId)
				.getResultList();
	}

	public List<WidgetOrderDto> findAllByTravelId(Long travelId) {
		return em.createQuery(
						"SELECT new"
							+ " com.genesis.unipocket.travel.query.persistence.response.WidgetOrderDto(w.widgetType,"
							+ " w.displayOrder) FROM TravelWidgetEntity w WHERE w.travelId ="
							+ " :travelId ORDER BY w.displayOrder ASC",
						WidgetOrderDto.class)
				.setParameter("travelId", travelId)
				.getResultList();
	}

	public boolean existsByAccountBookIdAndImageKey(Long accountBookId, String imageKey) {
		Long count =
				em.createQuery(
								"SELECT COUNT(t) FROM Travel t WHERE t.accountBookId = :accountBookId AND"
										+ " t.imageKey = :imageKey",
								Long.class)
						.setParameter("accountBookId", accountBookId)
						.setParameter("imageKey", imageKey)
						.getSingleResult();
		return count != null && count > 0;
	}
}
