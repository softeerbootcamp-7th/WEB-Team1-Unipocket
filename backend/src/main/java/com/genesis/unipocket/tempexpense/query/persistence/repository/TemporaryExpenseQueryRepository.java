package com.genesis.unipocket.tempexpense.query.persistence.repository;

import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseFileRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseItemRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaSummaryRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TemporaryExpenseQueryRepository {

	@PersistenceContext private EntityManager em;

	public List<TemporaryExpenseMetaSummaryRow> findMetaSummariesByAccountBookId(
			Long accountBookId) {
		return em.createQuery(
						"SELECT new com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaSummaryRow("
								+ "tm.tempExpenseMetaId, tm.createdAt, "
								+ "(SELECT COUNT(f.fileId) FROM File f WHERE f.tempExpenseMetaId = tm.tempExpenseMetaId), "
								+ "(SELECT COUNT(te.tempExpenseId) FROM TemporaryExpense te WHERE te.tempExpenseMetaId = tm.tempExpenseMetaId AND te.status = com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus.NORMAL), "
								+ "(SELECT COUNT(te.tempExpenseId) FROM TemporaryExpense te WHERE te.tempExpenseMetaId = tm.tempExpenseMetaId AND te.status = com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus.INCOMPLETE), "
								+ "(SELECT COUNT(te.tempExpenseId) FROM TemporaryExpense te WHERE te.tempExpenseMetaId = tm.tempExpenseMetaId AND te.status = com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus.ABNORMAL)) "
								+ "FROM TempExpenseMeta tm "
								+ "WHERE tm.accountBookId = :accountBookId "
								+ "ORDER BY tm.createdAt DESC",
						TemporaryExpenseMetaSummaryRow.class)
				.setParameter("accountBookId", accountBookId)
				.getResultList();
	}

	public Optional<TemporaryExpenseMetaRow> findMetaById(Long tempExpenseMetaId) {
		return em.createQuery(
						"SELECT new com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaRow("
								+ "tm.tempExpenseMetaId, tm.createdAt) "
								+ "FROM TempExpenseMeta tm "
								+ "WHERE tm.tempExpenseMetaId = :tempExpenseMetaId",
						TemporaryExpenseMetaRow.class)
				.setParameter("tempExpenseMetaId", tempExpenseMetaId)
				.getResultList()
				.stream()
				.findFirst();
	}

	public Optional<TemporaryExpenseMetaRow> findMetaInAccountBook(
			Long accountBookId, Long tempExpenseMetaId) {
		return em.createQuery(
						"SELECT new com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaRow("
								+ "tm.tempExpenseMetaId, tm.createdAt) "
								+ "FROM TempExpenseMeta tm "
								+ "WHERE tm.accountBookId = :accountBookId "
								+ "AND tm.tempExpenseMetaId = :tempExpenseMetaId",
						TemporaryExpenseMetaRow.class)
				.setParameter("accountBookId", accountBookId)
				.setParameter("tempExpenseMetaId", tempExpenseMetaId)
				.getResultList()
				.stream()
				.findFirst();
	}

	public List<TemporaryExpenseFileRow> findFilesByMetaId(Long tempExpenseMetaId) {
		return em.createQuery(
						"SELECT new com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseFileRow("
								+ "f.fileId, f.tempExpenseMetaId, f.s3Key, f.fileType) "
								+ "FROM File f "
								+ "WHERE f.tempExpenseMetaId = :tempExpenseMetaId "
								+ "ORDER BY f.fileId ASC",
						TemporaryExpenseFileRow.class)
				.setParameter("tempExpenseMetaId", tempExpenseMetaId)
				.getResultList();
	}

	public Optional<TemporaryExpenseFileRow> findScopedFile(
			Long accountBookId, Long tempExpenseMetaId, Long fileId) {
		return em.createQuery(
						"SELECT new com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseFileRow("
								+ "f.fileId, f.tempExpenseMetaId, f.s3Key, f.fileType) "
								+ "FROM File f "
								+ "JOIN TempExpenseMeta tm ON tm.tempExpenseMetaId = f.tempExpenseMetaId "
								+ "WHERE tm.accountBookId = :accountBookId "
								+ "AND tm.tempExpenseMetaId = :tempExpenseMetaId "
								+ "AND f.fileId = :fileId",
						TemporaryExpenseFileRow.class)
				.setParameter("accountBookId", accountBookId)
				.setParameter("tempExpenseMetaId", tempExpenseMetaId)
				.setParameter("fileId", fileId)
				.getResultList()
				.stream()
				.findFirst();
	}

	public List<TemporaryExpenseItemRow> findExpensesByFileIds(List<Long> fileIds) {
		if (fileIds.isEmpty()) {
			return List.of();
		}
		return em.createQuery(
						"SELECT new com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseItemRow("
								+ "te.tempExpenseId, te.tempExpenseMetaId, te.fileId, te.merchantName, te.category, "
								+ "te.localCountryCode, te.localCurrencyAmount, te.baseCountryCode, te.baseCurrencyAmount, "
								+ "te.paymentsMethod, te.memo, te.occurredAt, te.status, te.cardLastFourDigits) "
								+ "FROM TemporaryExpense te "
								+ "WHERE te.fileId IN :fileIds "
								+ "ORDER BY te.tempExpenseId ASC",
						TemporaryExpenseItemRow.class)
				.setParameter("fileIds", fileIds)
				.getResultList();
	}

	public List<TemporaryExpenseItemRow> findExpensesByFileId(Long fileId) {
		return em.createQuery(
						"SELECT new com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseItemRow("
								+ "te.tempExpenseId, te.tempExpenseMetaId, te.fileId, te.merchantName, te.category, "
								+ "te.localCountryCode, te.localCurrencyAmount, te.baseCountryCode, te.baseCurrencyAmount, "
								+ "te.paymentsMethod, te.memo, te.occurredAt, te.status, te.cardLastFourDigits) "
								+ "FROM TemporaryExpense te "
								+ "WHERE te.fileId = :fileId "
								+ "ORDER BY te.tempExpenseId ASC",
						TemporaryExpenseItemRow.class)
				.setParameter("fileId", fileId)
				.getResultList();
	}
}
