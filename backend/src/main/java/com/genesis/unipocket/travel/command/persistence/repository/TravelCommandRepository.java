package com.genesis.unipocket.travel.command.persistence.repository;

import com.genesis.unipocket.travel.command.persistence.entity.Travel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelCommandRepository extends JpaRepository<Travel, Long> {
	List<Travel> findAllByAccountBookId(Long accountBookId);
}
