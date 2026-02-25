package com.genesis.unipocket.accountbook.query.persistence.repository;

import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateSource;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

	public Optional<AccountBookDetailResponse> findDetailById(UUID userId, Long id) {
		List<AccountBookDetailResponse> result =
				em.createQuery(
								"SELECT new"
									+ " com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse("
									+ " a.id, a.title, a.localCountryCode, a.baseCountryCode,"
									+ " a.startDate, a.endDate) FROM AccountBookEntity a WHERE a.id"
									+ " = :id AND a.user.id = :userId",
								AccountBookDetailResponse.class)
						.setParameter("id", id)
						.setParameter("userId", userId)
						.getResultList();

		return result.stream().findFirst();
	}

	public List<AccountBookSummaryResponse> findAllByUserId(UUID userId) {

		return em.createQuery(
						"SELECT new"
							+ " com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse("
							+ " a.id, a.title, CASE WHEN a.id = (SELECT u.mainBucketId FROM"
							+ " UserEntity u WHERE u.id = :userId) THEN true ELSE false END) FROM"
							+ " AccountBookEntity a WHERE a.user.id = :userId ORDER BY CASE WHEN"
							+ " a.id = (SELECT u.mainBucketId FROM UserEntity u WHERE u.id ="
							+ " :userId) THEN 0 ELSE 1 END ASC, a.bucketOrder ASC",
						AccountBookSummaryResponse.class)
				.setParameter("userId", userId)
				.getResultList();
	}

	public Optional<AccountBookExchangeRateSource> findExchangeRateSourceById(
			UUID userId, Long id) {
		List<AccountBookExchangeRateSource> result =
				em.createQuery(
								"SELECT new"
									+ " com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateSource("
									+ " a.localCountryCode, a.baseCountryCode, a.budgetCreatedAt)"
									+ " FROM AccountBookEntity a WHERE a.id = :id AND a.user.id ="
									+ " :userId",
								AccountBookExchangeRateSource.class)
						.setParameter("id", id)
						.setParameter("userId", userId)
						.getResultList();

		return result.stream().findFirst();
	}

	public long countByUserId(UUID userId) {
		return em.createQuery(
						"SELECT COUNT(a.id) FROM AccountBookEntity a WHERE a.user.id = :userId",
						Long.class)
				.setParameter("userId", userId)
				.getSingleResult();
	}
}
