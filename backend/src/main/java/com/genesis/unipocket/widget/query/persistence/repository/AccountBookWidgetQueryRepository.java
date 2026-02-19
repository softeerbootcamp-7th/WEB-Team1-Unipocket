package com.genesis.unipocket.widget.query.persistence.repository;

import com.genesis.unipocket.widget.command.persistence.entity.AccountBookWidgetEntity;
import com.genesis.unipocket.widget.query.persistence.response.WidgetItemQueryRes;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountBookWidgetQueryRepository
		extends JpaRepository<AccountBookWidgetEntity, Long> {

	@Query(
			"SELECT new com.genesis.unipocket.widget.query.persistence.response.WidgetItemQueryRes("
					+ "e.displayOrder, e.widgetType, e.currencyType, e.period) "
					+ "FROM AccountBookWidgetEntity e "
					+ "WHERE e.accountBookId = :accountBookId "
					+ "ORDER BY e.displayOrder ASC")
	List<WidgetItemQueryRes> findAllByAccountBookId(@Param("accountBookId") Long accountBookId);
}
