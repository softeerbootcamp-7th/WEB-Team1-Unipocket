package com.genesis.unipocket.tempexpense.common.infrastructure.sse;

import com.genesis.unipocket.global.config.RedisPubSubConfig;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.event.ParsingErrorEvent;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.event.ParsingProgressEvent;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParsingProgressPublisher {

	private static final String TASK_KEY_PREFIX = "tempexpense:parse-task:";
	private static final String FIELD_ACCOUNT_BOOK_ID = "accountBookId";
	private static final String FIELD_PROGRESS = "progress";
	private static final String FIELD_STATUS = "status";
	private static final String FIELD_ERROR_CODE = "errorCode";
	private static final String FIELD_ERROR_STATUS = "errorStatus";
	private static final String FIELD_ERROR_MESSAGE = "errorMessage";
	private static final String FIELD_LAST_MESSAGE = "lastMessage";
	private static final String FIELD_LAST_CODE = "lastCode";
	private static final String FIELD_LAST_FILE_KEY = "lastFileKey";

	private final RedisTemplate<String, String> redisTemplate;
	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	@Value(
			"${tempexpense.parse-task.ttl-seconds:${tempexpense.parse-task.completed-ttl-seconds:600}}")
	private long taskTtlSeconds;

	public void registerTask(String taskId, Long accountBookId) {
		redisTemplate.delete(taskKey(taskId));
		saveStateToRedis(
				taskId,
				new ParsingTaskState(accountBookId, 0, TaskStatus.ACTIVE, null, null, null));
	}

	public boolean addEmitter(String taskId, Long accountBookId, SseEmitter emitter) {
		ParsingTaskState state = readTaskStateFromRedis(taskId);
		if (state == null
				|| state.status() != TaskStatus.ACTIVE
				|| !state.accountBookId().equals(accountBookId)) {
			return false;
		}

		emitters.put(taskId, emitter);
		sendProgress(taskId, emitter, state.progress(), null, null, null);
		return true;
	}

	public boolean replayTerminalIfPresent(String taskId, Long accountBookId, SseEmitter emitter) {
		ParsingTaskState state = readTaskStateFromRedis(taskId);
		if (state == null
				|| state.status() == TaskStatus.ACTIVE
				|| !state.accountBookId().equals(accountBookId)) {
			return false;
		}
		sendProgress(taskId, emitter, state.progress(), null, null, null);
		flushTerminalEvent(taskId, emitter, state);
		return true;
	}

	public void removeEmitter(String taskId) {
		emitters.remove(taskId);
	}

	public void publishProgress(String taskId, int progress) {
		publishProgress(taskId, progress, null, null, null);
	}

	public void publishProgress(String taskId, int progress, String message) {
		publishProgress(taskId, progress, message, null, null);
	}

	public void publishProgress(
			String taskId, int progress, String message, String code, String fileKey) {
		ParsingTaskState state = readTaskStateFromRedis(taskId);
		if (state == null || state.status() != TaskStatus.ACTIVE) {
			return;
		}

		int normalized = clamp(progress);
		saveStateToRedis(
				taskId,
				new ParsingTaskState(
						state.accountBookId(),
						normalized,
						TaskStatus.ACTIVE,
						null,
						null,
						null,
						message,
						code,
						fileKey));

		notifyViaChannel(taskId);
	}

	public void complete(String taskId) {
		ParsingTaskState state = readTaskStateFromRedis(taskId);
		if (state == null || state.status() != TaskStatus.ACTIVE) {
			return;
		}

		ParsingTaskState terminalState =
				new ParsingTaskState(
						state.accountBookId(),
						100,
						TaskStatus.COMPLETE,
						null,
						null,
						null,
						state.lastMessage(),
						state.lastCode(),
						state.lastFileKey());
		saveStateToRedis(taskId, terminalState);

		notifyViaChannel(taskId);
	}

	public void publishError(String taskId, ErrorCode errorCode) {
		publishError(taskId, errorCode, errorCode.getMessage());
	}

	public void publishError(String taskId, ErrorCode errorCode, String errorMessage) {
		publishError(taskId, errorCode.getCode(), errorCode.getStatus().value(), errorMessage);
	}

	public void publishError(String taskId, String errorMessage) {
		publishError(taskId, null, null, errorMessage);
	}

	private void publishError(
			String taskId, String errorCode, Integer errorStatus, String errorMessage) {
		ParsingTaskState state = readTaskStateFromRedis(taskId);
		if (state == null || state.status() != TaskStatus.ACTIVE) {
			return;
		}

		ParsingTaskState terminalState =
				new ParsingTaskState(
						state.accountBookId(),
						state.progress(),
						TaskStatus.ERROR,
						errorCode,
						errorStatus,
						errorMessage,
						state.lastMessage(),
						state.lastCode(),
						state.lastFileKey());
		saveStateToRedis(taskId, terminalState);

		notifyViaChannel(taskId);
	}

	private ParsingTaskState readTaskStateFromRedis(String taskId) {
		HashOperations<String, String, String> hash = redisTemplate.opsForHash();
		String key = taskKey(taskId);

		String accountBookId = hash.get(key, FIELD_ACCOUNT_BOOK_ID);
		if (accountBookId == null) {
			return null;
		}

		String progressValue = hash.get(key, FIELD_PROGRESS);
		String statusValue = hash.get(key, FIELD_STATUS);
		String errorCode = hash.get(key, FIELD_ERROR_CODE);
		String errorStatusValue = hash.get(key, FIELD_ERROR_STATUS);
		String errorMessage = hash.get(key, FIELD_ERROR_MESSAGE);
		String lastMessage = hash.get(key, FIELD_LAST_MESSAGE);
		String lastCode = hash.get(key, FIELD_LAST_CODE);
		String lastFileKey = hash.get(key, FIELD_LAST_FILE_KEY);

		Long parsedAccountBookId = Long.valueOf(accountBookId);
		int progress = parseProgress(progressValue);
		TaskStatus status = parseStatus(statusValue);
		Integer parsedErrorStatus = parseErrorStatus(errorStatusValue);
		return new ParsingTaskState(
				parsedAccountBookId,
				progress,
				status,
				errorCode,
				parsedErrorStatus,
				errorMessage,
				lastMessage,
				lastCode,
				lastFileKey);
	}

	private void saveStateToRedis(String taskId, ParsingTaskState state) {
		HashOperations<String, String, String> hash = redisTemplate.opsForHash();
		String key = taskKey(taskId);
		hash.put(key, FIELD_ACCOUNT_BOOK_ID, String.valueOf(state.accountBookId()));
		hash.put(key, FIELD_PROGRESS, String.valueOf(clamp(state.progress())));
		hash.put(key, FIELD_STATUS, state.status().name());
		putOrDelete(hash, key, FIELD_ERROR_CODE, state.errorCode());
		putOrDelete(hash, key, FIELD_ERROR_MESSAGE, state.errorMessage());
		putOrDelete(hash, key, FIELD_LAST_MESSAGE, state.lastMessage());
		putOrDelete(hash, key, FIELD_LAST_CODE, state.lastCode());
		putOrDelete(hash, key, FIELD_LAST_FILE_KEY, state.lastFileKey());
		if (state.errorStatus() == null) {
			hash.delete(key, FIELD_ERROR_STATUS);
		} else {
			hash.put(key, FIELD_ERROR_STATUS, String.valueOf(state.errorStatus()));
		}
		applyTtl(key);
	}

	private void putOrDelete(
			HashOperations<String, String, String> hash, String key, String field, String value) {
		if (value == null || value.isBlank()) {
			hash.delete(key, field);
		} else {
			hash.put(key, field, value);
		}
	}

	private void applyTtl(String key) {
		if (taskTtlSeconds > 0) {
			redisTemplate.expire(key, taskTtlSeconds, TimeUnit.SECONDS);
		}
	}

	private void sendProgress(
			String taskId,
			SseEmitter emitter,
			int progress,
			String message,
			String code,
			String fileKey) {
		boolean sent =
				sendEvent(
						emitter,
						taskId,
						"progress",
						new ParsingProgressEvent(progress, message, code, fileKey));
		if (!sent) {
			emitters.remove(taskId);
		}
	}

	private void flushTerminalEvent(String taskId, SseEmitter emitter, ParsingTaskState state) {
		boolean sent =
				state.status() == TaskStatus.ERROR
						? sendEvent(
								emitter,
								taskId,
								"error",
								new ParsingErrorEvent(
										state.errorMessage(),
										state.errorCode(),
										state.errorStatus(),
										state.lastCode(),
										state.lastFileKey()))
						: sendEvent(
								emitter,
								taskId,
								"complete",
								new ParsingProgressEvent(
										state.progress(),
										state.lastMessage(),
										state.lastCode(),
										state.lastFileKey()));
		if (!sent) {
			emitters.remove(taskId);
			return;
		}

		try {
			emitter.complete();
		} finally {
			emitters.remove(taskId);
		}
	}

	private boolean sendEvent(SseEmitter emitter, String taskId, String eventName, Object data) {
		try {
			emitter.send(SseEmitter.event().name(eventName).data(data));
			return true;
		} catch (IOException | IllegalStateException e) {
			log.warn("Failed to send {} event. taskId={}", eventName, taskId, e);
			return false;
		}
	}

	// ── Redis Pub/Sub ──────────────────────────────────────────

	private void notifyViaChannel(String taskId) {
		try {
			redisTemplate.convertAndSend(RedisPubSubConfig.PARSE_NOTIFY_CHANNEL, taskId);
		} catch (Exception e) {
			log.warn("Failed to publish Pub/Sub notification. taskId={}", taskId, e);
			// Pub/Sub 실패 시 로컬 emitter로 직접 전달 (단일 인스턴스 폴백)
			deliverToLocalEmitter(taskId);
		}
	}

	/**
	 * Redis Pub/Sub 메시지 수신 핸들러.
	 * RedisPubSubConfig의 MessageListenerAdapter가 이 메서드를 호출한다.
	 * 모든 인스턴스가 수신하지만, 로컬에 해당 taskId의 emitter가 있는 인스턴스만 처리한다.
	 */
	public void onPubSubNotification(String taskId) {
		deliverToLocalEmitter(taskId);
	}

	private void deliverToLocalEmitter(String taskId) {
		SseEmitter emitter = emitters.get(taskId);
		if (emitter == null) {
			return; // 이 인스턴스에 해당 emitter가 없으면 무시
		}

		ParsingTaskState state = readTaskStateFromRedis(taskId);
		if (state == null) {
			return;
		}

		if (state.status() == TaskStatus.ACTIVE) {
			sendProgress(
					taskId,
					emitter,
					state.progress(),
					state.lastMessage(),
					state.lastCode(),
					state.lastFileKey());
		} else {
			flushTerminalEvent(taskId, emitter, state);
		}
	}

	// ── Helpers ────────────────────────────────────────────────

	private String taskKey(String taskId) {
		return TASK_KEY_PREFIX + taskId;
	}

	private int parseProgress(String value) {
		if (value == null) {
			return 0;
		}
		try {
			return clamp(Integer.parseInt(value));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private TaskStatus parseStatus(String value) {
		if (value == null || value.isBlank()) {
			return TaskStatus.ACTIVE;
		}
		try {
			return TaskStatus.valueOf(value);
		} catch (IllegalArgumentException e) {
			return TaskStatus.ACTIVE;
		}
	}

	private Integer parseErrorStatus(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private int clamp(int progress) {
		return Math.max(0, Math.min(100, progress));
	}
}
