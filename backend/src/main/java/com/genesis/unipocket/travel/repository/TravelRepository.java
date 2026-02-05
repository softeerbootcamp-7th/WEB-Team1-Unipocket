package com.genesis.unipocket.travel.repository;

import com.genesis.unipocket.travel.domain.Travel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelRepository extends JpaRepository<Travel, Long> {
	List<Travel> findAllByAccountBookId(Long accountBookId);
}
