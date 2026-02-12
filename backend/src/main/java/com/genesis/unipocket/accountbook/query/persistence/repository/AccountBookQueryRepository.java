package com.genesis.unipocket.accountbook.query.persistence.repository;

import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountBookQueryRepository {

	@PersistenceContext private final EntityManager em;

	public Optional<AccountBookQueryResponse> findById(Long id) {
		List<AccountBookQueryResponse> result =
				em.createQuery(
								"SELECT new"
									+ " com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse("
									+ " a.id, a.title, a.localCountryCode, a.baseCountryCode,"
									+ " a.startDate, a.endDate) FROM AccountBookEntity a WHERE a.id"
									+ " = :id",
								AccountBookQueryResponse.class)
						.setParameter("id", id)
						.getResultList();

		return result.stream().findFirst();
	}

	public Optional<AccountBookDetailResponse> findDetailById(String userId, Long id) {
		// TODO: tempExpenseBatchIds is currently empty list, need to implement logic if
		// needed
		List<AccountBookDetailResponse> result =
				em.createQuery(
								"SELECT new"
									+ " com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse("
									+ " a.id, a.title, a.localCountryCode, a.baseCountryCode,"
									+ " a.budget, a.budgetCreatedAt, null, a.startDate, a.endDate)"
									+ " FROM AccountBookEntity a WHERE a.id = :id AND a.userId ="
									+ " :userId",
								AccountBookDetailResponse.class)
						.setParameter("id", id)
						.setParameter("userId", userId)
						.getResultList();

		return result.stream().findFirst();
	}

	public List<AccountBookSummaryResponse> findAllByUserId(String userId, Long mainAccountBookId) {
		// Note: isMain logic is calculated in memory or query if possible. Here
		// simplified.
		// Assuming we pass mainAccountBookId to determine isMain in the service or
		// here.
		// But in JPQL we can't easily compare with external value for boolean result in
		// projection
		// without CASE.
		// Let's return basics and map in service, OR use CASE WHEN.

		return em.createQuery(
						"SELECT new"
							+ " com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse("
							+ " a.id, a.title, CASE WHEN a.id = :mainId THEN true ELSE false END)"
							+ " FROM AccountBookEntity a WHERE a.userId = :userId",
						AccountBookSummaryResponse.class)
				.setParameter("userId", userId)
				.setParameter("mainId", mainAccountBookId)
				.getResultList();
	}
}
