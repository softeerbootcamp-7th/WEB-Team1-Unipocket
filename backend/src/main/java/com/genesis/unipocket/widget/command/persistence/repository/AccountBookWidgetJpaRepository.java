package com.genesis.unipocket.widget.command.persistence.repository;

import com.genesis.unipocket.widget.command.persistence.entity.AccountBookWidgetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountBookWidgetJpaRepository
		extends JpaRepository<AccountBookWidgetEntity, Long> {

	void deleteAllByAccountBookId(Long accountBookId);

	List<AccountBookWidgetEntity> findAllByAccountBookIdOrderByDisplayOrderAsc(Long accountBookId);
}
