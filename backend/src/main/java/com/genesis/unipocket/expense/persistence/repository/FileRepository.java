package com.genesis.unipocket.expense.persistence.repository;

import com.genesis.unipocket.expense.persistence.entity.expense.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <b>파일 Repository</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {}
