package com.genesis.unipocket.travel.persistence.repository;

import com.genesis.unipocket.travel.persistence.entity.TravelWidget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelWidgetRepository extends JpaRepository<TravelWidget, Long> {
	List<TravelWidget> findAllByTravelIdOrderByWidgetOrderAsc(Long travelId);

	void deleteAllByTravelId(Long travelId);
}
