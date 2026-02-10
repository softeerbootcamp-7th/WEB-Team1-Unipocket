package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.expense.command.persistence.entity.expense.File;
import com.genesis.unipocket.expense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TempExpenseMetaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>미사용 업로드 정리 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemporaryExpenseCleanupService {

	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;

	@Value("${temp-expense.cleanup.retention-hours:24}")
	private int retentionHours;

	@Scheduled(cron = "${temp-expense.cleanup.cron:0 0 * * * *}")
	@Transactional
	public void cleanupStaleUploads() {
		LocalDateTime cutoff = LocalDateTime.now().minusHours(retentionHours);
		List<File> staleFiles = fileRepository.findStaleUnparsedFiles(cutoff);

		if (staleFiles.isEmpty()) {
			log.debug("No stale uploads to cleanup");
			return;
		}

		for (File file : staleFiles) {
			fileRepository.delete(file);
			tempExpenseMetaRepository.deleteById(file.getTempExpenseMetaId());
		}

		log.info("Cleaned up {} stale uploads", staleFiles.size());
	}
}
