package com.genesis.unipocket.travel.repository;

import com.genesis.unipocket.travel.domain.TravelWidget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelWidgetRepository extends JpaRepository<TravelWidget, Long> {
	List<TravelWidget> findAllByTravelIdOrderByWidgetOrderAsc(Long travelId);
}
