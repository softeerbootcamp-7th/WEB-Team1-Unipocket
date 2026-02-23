package com.genesis.unipocket.user.command.persistence.repository;

import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCardCommandRepository extends JpaRepository<UserCardEntity, Long> {
	List<UserCardEntity> findAllByUser_Id(UUID userId);

	Optional<UserCardEntity> findByUserCardIdAndUser_Id(Long userCardId, UUID userId);

	long countByUser(UserEntity user);
}
