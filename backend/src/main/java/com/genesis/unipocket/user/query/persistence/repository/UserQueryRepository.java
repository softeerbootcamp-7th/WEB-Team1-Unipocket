package com.genesis.unipocket.user.query.persistence.repository;

import com.genesis.unipocket.user.query.persistence.response.UserCardQueryResponse;
import com.genesis.unipocket.user.query.persistence.response.UserQueryResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserQueryRepository {

	@PersistenceContext private EntityManager em;

	public Optional<UserQueryResponse> findById(UUID id) {
		List<UserQueryResponse> result =
				em.createQuery(
								"SELECT new"
									+ " com.genesis.unipocket.user.query.persistence.response.UserQueryResponse(u.id,"
									+ " u.email, u.name, u.profileImgUrl, u.role, u.status) FROM"
									+ " UserEntity u WHERE u.id = :id",
								UserQueryResponse.class)
						.setParameter("id", id)
						.getResultList();
		return result.stream().findFirst();
	}

	public List<UserCardQueryResponse> findAllCardsByUserId(UUID userId) {
		return em.createQuery(
						"SELECT new"
							+ " com.genesis.unipocket.user.query.persistence.response.UserCardQueryResponse(c.userCardId,"
							+ " c.nickName, c.cardNumber, c.cardCompany) FROM UserCardEntity c"
							+ " WHERE c.user.id = :userId",
						UserCardQueryResponse.class)
				.setParameter("userId", userId)
				.getResultList();
	}
}
