package com.genesis.unipocket.expense.infrastructure;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * <b>파싱 진행 상황 SSE Publisher</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Component
public class ParsingProgressPublisher {

	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	/**
	 * SSE Emitter 등록
	 */
	public void addEmitter(String taskId, SseEmitter emitter) {
		emitters.put(taskId, emitter);
		log.info("Added SSE emitter for task: {}", taskId);
	}

	/**
	 * SSE Emitter 제거
	 */
	public void removeEmitter(String taskId) {
		emitters.remove(taskId);
		log.info("Removed SSE emitter for task: {}", taskId);
	}

	/**
	 * 진행 상황 전송
	 */
	public void publishProgress(String taskId, ParsingProgressEvent event) {
		SseEmitter emitter = emitters.get(taskId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name("progress").data(event));
				log.debug(
						"Published progress for task {}: {}/{}",
						taskId,
						event.completedFiles(),
						event.totalFiles());
			} catch (IOException e) {
				log.error("Failed to send progress event for task: {}", taskId, e);
				emitters.remove(taskId);
			}
		}
	}

	/**
	 * 완료 이벤트 전송
	 */
	public void complete(String taskId, Object result) {
		SseEmitter emitter = emitters.remove(taskId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name("complete").data(result));
				emitter.complete();
				log.info("Published complete event for task: {}", taskId);
			} catch (IOException e) {
				log.error("Failed to send complete event for task: {}", taskId, e);
			}
		}
	}

	/**
	 * 에러 이벤트 전송
	 */
	public void publishError(String taskId, String errorMessage) {
		SseEmitter emitter = emitters.remove(taskId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name("error").data(Map.of("error", errorMessage)));
				emitter.completeWithError(new RuntimeException(errorMessage));
				log.error("Published error event for task {}: {}", taskId, errorMessage);
			} catch (IOException e) {
				log.error("Failed to send error event for task: {}", taskId, e);
			}
		}
	}

	/**
	 * 진행 상황 이벤트
	 */
	public record ParsingProgressEvent(
			int completedFiles, int totalFiles, String currentFileName, int progress) {}
}
