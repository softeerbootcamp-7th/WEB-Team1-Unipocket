package com.genesis.unipocket.user.command.persistence.repository;

import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCardRepository extends JpaRepository<UserCardEntity, Long> {
	List<UserCardEntity> findAllByUser_Id(UUID userId);
}
