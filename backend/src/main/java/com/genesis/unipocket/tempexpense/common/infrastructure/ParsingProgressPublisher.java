package com.genesis.unipocket.tempexpense.common.infrastructure;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * <b>파싱 진행 상황 SSE Publisher</b>
 */
@Slf4j
@Component
public class ParsingProgressPublisher {

	/** 구독 전 파일 에러 버퍼 최대 개수 */
	private static final int MAX_BUFFERED_FILE_ERRORS_PER_TASK = 30;

	/** 구독 없이 종료된 task 종료 이벤트 보관 시간 */
	private static final Duration TERMINAL_EVENT_RETENTION = Duration.ofMinutes(30);
	/** 비구독 활성 task 보관 시간 (비정상 종료 누수 방지) */
	private static final Duration ACTIVE_TASK_RETENTION = Duration.ofHours(6);

	/** task 단위 SSE 상태 저장소 */
	private final Map<String, TaskContext> tasks = new ConcurrentHashMap<>();

	/** task 등록 */
	public void registerTask(String taskId, Long accountBookId) {
		purgeExpiredTerminalTasks();
		tasks.put(taskId, new TaskContext(accountBookId));
	}

	/** task 소유권 검증 */
	public boolean isTaskOwnedBy(String taskId, Long accountBookId) {
		TaskContext taskContext = tasks.get(taskId);
		return taskContext != null && taskContext.isOwnedBy(accountBookId);
	}

	/** emitter 등록 후 최신 progress/file-error/terminal을 즉시 flush */
	public void addEmitter(String taskId, SseEmitter emitter) {
		purgeExpiredTerminalTasks();

		TaskContext taskContext = tasks.get(taskId);
		if (taskContext == null) {
			log.warn("Task not found while adding emitter. taskId={}", taskId);
			emitter.complete();
			return;
		}

		taskContext.attachEmitter(emitter);
		flushBufferedState(taskId, taskContext, emitter);
		log.info("Added SSE emitter for task: {}", taskId);
	}

	/** emitter 연결만 제거 */
	public void removeEmitter(String taskId) {
		TaskContext taskContext = tasks.get(taskId);
		if (taskContext == null) {
			return;
		}
		taskContext.detachEmitter();
		if (taskContext.hasTerminalEvent()) {
			cleanupTask(taskId);
		}
		log.info("Removed SSE emitter for task: {}", taskId);
	}

	/** progress 전송, emitter가 없으면 최신 상태만 저장 */
	public void publishProgress(String taskId, ParsingProgressEvent event) {
		TaskContext taskContext = tasks.get(taskId);
		if (taskContext == null) {
			log.warn("Task not found while publishing progress. taskId={}", taskId);
			return;
		}

		taskContext.updateLatestProgress(event);
		SseEmitter emitter = taskContext.currentEmitter();
		if (emitter != null && !sendEvent(emitter, taskId, "progress", event)) {
			taskContext.detachEmitter();
		}

		log.debug(
				"Published progress for task {}: {}/{}",
				taskId,
				event.completedFiles(),
				event.totalFiles());
	}

	/** file-error 전송, emitter가 없으면 버퍼링 */
	public void publishFileError(String taskId, FileErrorEvent event) {
		TaskContext taskContext = tasks.get(taskId);
		if (taskContext == null) {
			log.warn("Task not found while publishing file-error. taskId={}", taskId);
			return;
		}

		SseEmitter emitter = taskContext.currentEmitter();
		if (emitter != null) {
			if (!sendEvent(emitter, taskId, "file-error", event)) {
				taskContext.detachEmitter();
				taskContext.bufferFileError(event);
			}
		} else {
			taskContext.bufferFileError(event);
		}

		log.warn(
				"Published file error for task {}: file={}, message={}",
				taskId,
				event.fileKey(),
				event.message());
	}

	/** complete 전송, 구독 전 종료면 terminal로 저장 */
	public void complete(String taskId, Object result) {
		TaskContext taskContext = tasks.get(taskId);
		if (taskContext == null) {
			log.warn("Task not found while publishing complete. taskId={}", taskId);
			return;
		}

		TerminalSsePayload terminal = new TerminalSsePayload("complete", result, null);
		taskContext.markTerminalEvent(terminal);

		SseEmitter emitter = taskContext.currentEmitter();
		if (emitter != null) {
			flushTerminalEvent(taskId, taskContext, emitter, terminal);
			return;
		}

		log.info("Stored complete terminal event for late subscriber. taskId={}", taskId);
	}

	/** error 전송, 구독 전 종료면 terminal로 저장 */
	public void publishError(String taskId, String errorMessage) {
		TaskContext taskContext = tasks.get(taskId);
		if (taskContext == null) {
			log.warn(
					"Task not found while publishing error. taskId={}, error={}",
					taskId,
					errorMessage);
			return;
		}

		TerminalSsePayload terminal =
				new TerminalSsePayload("error", Map.of("error", errorMessage), errorMessage);
		taskContext.markTerminalEvent(terminal);

		SseEmitter emitter = taskContext.currentEmitter();
		if (emitter != null) {
			flushTerminalEvent(taskId, taskContext, emitter, terminal);
			return;
		}

		log.error(
				"Stored error event for late subscriber. taskId={}, error={}",
				taskId,
				errorMessage);
	}

	/** emitter 등록 시 누락된 최신 상태를 복원 전송 */
	private void flushBufferedState(String taskId, TaskContext taskContext, SseEmitter emitter) {
		ParsingProgressEvent latestProgress = taskContext.latestProgress();
		if (latestProgress != null && !sendEvent(emitter, taskId, "progress", latestProgress)) {
			taskContext.detachEmitter();
			return;
		}

		for (FileErrorEvent fileError : taskContext.drainBufferedFileErrors()) {
			if (!sendEvent(emitter, taskId, "file-error", fileError)) {
				taskContext.detachEmitter();
				taskContext.bufferFileError(fileError);
				return;
			}
		}

		TerminalSsePayload terminal = taskContext.terminalEvent();
		if (terminal != null) {
			flushTerminalEvent(taskId, taskContext, emitter, terminal);
		}
	}

	/** terminal complete/error 전송 후 task 정리 */
	private void flushTerminalEvent(
			String taskId,
			TaskContext taskContext,
			SseEmitter emitter,
			TerminalSsePayload terminal) {
		if (!sendEvent(emitter, taskId, terminal.name(), terminal.data())) {
			taskContext.detachEmitter();
			return;
		}

		try {
			if (terminal.errorMessage() != null) {
				emitter.completeWithError(new RuntimeException(terminal.errorMessage()));
			} else {
				emitter.complete();
			}
		} finally {
			taskContext.detachEmitter();
			cleanupTask(taskId);
		}
	}

	/** emitter로 이벤트 전송 */
	private boolean sendEvent(SseEmitter emitter, String taskId, String eventName, Object data) {
		try {
			emitter.send(SseEmitter.event().name(eventName).data(data));
			return true;
		} catch (IOException e) {
			log.error("Failed to send {} event for task: {}", eventName, taskId, e);
			return false;
		}
	}

	/** task 관련 캐시 정리 */
	private void cleanupTask(String taskId) {
		tasks.remove(taskId);
	}

	/** 미구독 종료 task를 보관 시간 이후 정리 */
	private void purgeExpiredTerminalTasks() {
		Instant terminalCutoff = Instant.now().minus(TERMINAL_EVENT_RETENTION);
		Instant activeCutoff = Instant.now().minus(ACTIVE_TASK_RETENTION);
		tasks.entrySet()
				.removeIf(
						entry ->
								entry.getValue().isExpiredTerminal(terminalCutoff)
										|| entry.getValue().isExpiredActiveWithoutEmitter(activeCutoff));
	}

	/** 진행 상황 이벤트 */
	public record ParsingProgressEvent(
			int completedFiles, int totalFiles, String currentFileName, int progress) {}

	/** 파일 단위 실패 이벤트 */
	public record FileErrorEvent(
			int completedFiles, int totalFiles, String fileKey, int progress, String message) {}

	private record TerminalSsePayload(String name, Object data, String errorMessage) {}

	private static final class TaskContext {
		private final Long accountBookId;
		private final Queue<FileErrorEvent> bufferedFileErrors = new ArrayDeque<>();
		private SseEmitter emitter;
		private ParsingProgressEvent latestProgress;
		private TerminalSsePayload terminalEvent;
		private Instant updatedAt = Instant.now();

		private TaskContext(Long accountBookId) {
			this.accountBookId = accountBookId;
		}

		/** task 소유 가계부 확인 */
		private synchronized boolean isOwnedBy(Long targetAccountBookId) {
			return accountBookId.equals(targetAccountBookId);
		}

		/** emitter 연결 */
		private synchronized void attachEmitter(SseEmitter sseEmitter) {
			this.emitter = sseEmitter;
			touch();
		}

		/** emitter 해제 */
		private synchronized void detachEmitter() {
			this.emitter = null;
			touch();
		}

		/** 현재 emitter 조회 */
		private synchronized SseEmitter currentEmitter() {
			return emitter;
		}

		/** 최신 progress 스냅샷 저장 */
		private synchronized void updateLatestProgress(ParsingProgressEvent event) {
			this.latestProgress = event;
			touch();
		}

		/** 최신 progress 조회 */
		private synchronized ParsingProgressEvent latestProgress() {
			return latestProgress;
		}

		/** file-error 버퍼링 (최대 개수 유지) */
		private synchronized void bufferFileError(FileErrorEvent event) {
			bufferedFileErrors.offer(event);
			while (bufferedFileErrors.size() > MAX_BUFFERED_FILE_ERRORS_PER_TASK) {
				bufferedFileErrors.poll();
			}
			touch();
		}

		/** 버퍼된 file-error 일괄 반환 */
		private synchronized List<FileErrorEvent> drainBufferedFileErrors() {
			List<FileErrorEvent> drained = new ArrayList<>(bufferedFileErrors);
			bufferedFileErrors.clear();
			touch();
			return drained;
		}

		/** 종료 이벤트 저장 */
		private synchronized void markTerminalEvent(TerminalSsePayload terminal) {
			this.terminalEvent = terminal;
			touch();
		}

		/** 종료 이벤트 조회 */
		private synchronized TerminalSsePayload terminalEvent() {
			return terminalEvent;
		}

		/** 종료 상태 여부 */
		private synchronized boolean hasTerminalEvent() {
			return terminalEvent != null;
		}

		/** 최근 갱신 시각 조회 */
		private synchronized Instant updatedAt() {
			return updatedAt;
		}

		/** 만료된 terminal 상태인지 확인 */
		private synchronized boolean isExpiredTerminal(Instant cutoff) {
			return terminalEvent != null && emitter == null && updatedAt.isBefore(cutoff);
		}

		/** 비구독 상태로 오래 방치된 활성 task인지 확인 */
		private synchronized boolean isExpiredActiveWithoutEmitter(Instant cutoff) {
			return terminalEvent == null && emitter == null && updatedAt.isBefore(cutoff);
		}

		private synchronized void touch() {
			updatedAt = Instant.now();
		}
	}
}
