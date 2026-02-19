package com.genesis.unipocket.media.command.application;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.media.command.facade.port.MediaUsedPathProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediaOrphanedFileCleanWorkerTest {

	@Mock private MediaObjectStorage mediaObjectStorage;
	@Mock private MediaUsedPathProvider provider1;
	@Mock private MediaUsedPathProvider provider2;

	@Test
	@DisplayName("사용 중이지 않은 키만 삭제한다")
	void clean_deletesOnlyOrphanedKeys() {
		MediaOrphanedFileCleanWorker worker =
				new MediaOrphanedFileCleanWorker(mediaObjectStorage, List.of(provider1, provider2));
		when(mediaObjectStorage.listAllKeys()).thenReturn(List.of("a", "b", "c"));
		when(provider1.getUsedPaths()).thenReturn(Set.of("a"));
		when(provider2.getUsedPaths()).thenReturn(Set.of("c"));

		worker.clean();

		verify(mediaObjectStorage).deleteObjects(List.of("b"));
	}

	@Test
	@DisplayName("삭제 대상이 없으면 deleteObjects를 호출하지 않는다")
	void clean_whenNoOrphans_doNotDelete() {
		MediaOrphanedFileCleanWorker worker =
				new MediaOrphanedFileCleanWorker(mediaObjectStorage, List.of(provider1, provider2));
		when(mediaObjectStorage.listAllKeys()).thenReturn(List.of("a", "b"));
		when(provider1.getUsedPaths()).thenReturn(Set.of("a", "b"));
		when(provider2.getUsedPaths()).thenReturn(Set.of());

		worker.clean();

		verify(mediaObjectStorage, never()).deleteObjects(anyList());
	}

	@Test
	@DisplayName("used path에 null/blank가 있어도 무시한다")
	void clean_ignoresNullAndBlankUsedPaths() {
		MediaOrphanedFileCleanWorker worker =
				new MediaOrphanedFileCleanWorker(mediaObjectStorage, List.of(provider1, provider2));
		when(mediaObjectStorage.listAllKeys()).thenReturn(List.of("a", "b"));
		when(provider1.getUsedPaths()).thenReturn(Set.of("a", " ", ""));
		when(provider2.getUsedPaths()).thenReturn(new HashSet<>(Arrays.asList((String) null)));

		worker.clean();

		verify(mediaObjectStorage).deleteObjects(List.of("b"));
	}
}
