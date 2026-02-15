package com.genesis.unipocket.travel.command.persistence.repository;

import com.genesis.unipocket.travel.command.persistence.entity.TravelWidget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelWidgetCommandRepository extends JpaRepository<TravelWidget, Long> {
	List<TravelWidget> findAllByTravelIdOrderByWidgetOrderAsc(Long travelId);

	void deleteAllByTravelId(Long travelId);
}
