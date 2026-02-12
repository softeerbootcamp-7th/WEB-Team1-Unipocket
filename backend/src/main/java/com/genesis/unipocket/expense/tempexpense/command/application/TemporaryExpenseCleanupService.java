package com.genesis.unipocket.expense.tempexpense.command.application;

import com.genesis.unipocket.expense.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.expense.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
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
 * <p>
 * S3 리소스 지우는 서비스가 아닌, <br>
 * 파싱 후 일정 시간이 지난 임시 지출내역 데이터들을 지워주는 서비스
 * </p>
 * @author bluefishez
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
