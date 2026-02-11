package com.genesis.unipocket.media.command.application;

import com.genesis.unipocket.media.command.facade.port.MediaObjectStoragePort;
import com.genesis.unipocket.media.command.facade.port.MediaUsedPathProvider;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <b>사용되지 않는 미디어 파일 삭제 워커 클래스</b>
 * <p>
 *   사용되지 않는 미디어 파일들에 대한 삭제 작업을 수행합니다.
 * </p>
 *
 * @author bluefishez
 * @since 2026-02-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaOrphanedFileCleanWorker {

	private final MediaObjectStoragePort mediaObjectStoragePort;
	private final List<MediaUsedPathProvider> mediaUsedPathProviders;

	@Scheduled(cron = "${media.cleanup.cron:0 * * * * *}")
	public void clean() {
		List<String> allKeys = mediaObjectStoragePort.listAllKeys();
		Set<String> usedKeys =
				mediaUsedPathProviders.stream()
						.map(MediaUsedPathProvider::getUsedPaths)
						.flatMap(Set::stream)
						.filter(path -> path != null && !path.isBlank())
						.collect(Collectors.toSet());

		List<String> targetKeys = identifyOrphanedKeys(allKeys, usedKeys);
		if (targetKeys.isEmpty()) {
			return;
		}

		mediaObjectStoragePort.deleteObjects(targetKeys);
		log.info("{} 개의 사용되지 않는 미디어 파일을 삭제했습니다.", targetKeys.size());
	}

	private List<String> identifyOrphanedKeys(List<String> allKeys, Set<String> usedKeys) {
		return allKeys.stream().filter(key -> !usedKeys.contains(key)).toList();
	}
}
