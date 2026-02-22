package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

		Set<Long> affectedMetaIds = new HashSet<>();
		for (File file : staleFiles) {
			fileRepository.delete(file);
			affectedMetaIds.add(file.getTempExpenseMetaId());
		}

		for (Long metaId : affectedMetaIds) {
			if (!fileRepository.existsByTempExpenseMetaId(metaId)) {
				tempExpenseMetaRepository.deleteById(metaId);
			}
		}

		log.info("Cleaned up {} stale uploads", staleFiles.size());
	}
}
