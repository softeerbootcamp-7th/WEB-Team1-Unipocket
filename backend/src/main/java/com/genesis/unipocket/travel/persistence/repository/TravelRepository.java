package com.genesis.unipocket.travel.persistence.repository;

import com.genesis.unipocket.travel.persistence.entity.Travel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelRepository extends JpaRepository<Travel, Long> {
	List<Travel> findAllByAccountBookId(Long accountBookId);
}
