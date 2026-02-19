package com.genesis.unipocket.widget.command.persistence.repository;

import com.genesis.unipocket.widget.command.persistence.entity.TravelWidgetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelWidgetJpaRepository extends JpaRepository<TravelWidgetEntity, Long> {

	void deleteAllByTravelId(Long travelId);

	List<TravelWidgetEntity> findAllByTravelIdOrderByDisplayOrderAsc(Long travelId);
}
