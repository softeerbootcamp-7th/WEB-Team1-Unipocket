package com.genesis.unipocket.tempexpense.common.infrastructure.sse;

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
	private static final String FIELD_ERROR_MESSAGE = "errorMessage";

	private final RedisTemplate<String, String> redisTemplate;
	private final Map<String, ParsingTaskState> activeTasks = new ConcurrentHashMap<>();
	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	@Value("${tempexpense.parse-task.completed-ttl-seconds:600}")
	private long completedTaskTtlSeconds;

	public void registerTask(String taskId, Long accountBookId) {
		redisTemplate.delete(taskKey(taskId));
		activeTasks.put(taskId, new ParsingTaskState(accountBookId, 0, TaskStatus.ACTIVE, null));
	}

	public boolean canAddEmitter(String taskId, Long accountBookId) {
		ParsingTaskState activeState = activeTasks.get(taskId);
		return activeState != null && activeState.accountBookId().equals(accountBookId);
	}

	public boolean addEmitter(String taskId, Long accountBookId, SseEmitter emitter) {
		if (!canAddEmitter(taskId, accountBookId)) {
			return false;
		}

		ParsingTaskState activeState = activeTasks.get(taskId);
		if (activeState == null) {
			return false;
		}

		emitters.put(taskId, emitter);
		sendProgress(taskId, emitter, activeState.progress());
		return true;
	}

	public boolean replayTerminalIfPresent(String taskId, Long accountBookId, SseEmitter emitter) {
		ParsingTaskState terminalState = readTaskStateFromRedis(taskId);
		if (terminalState == null
				|| terminalState.status() == TaskStatus.ACTIVE
				|| !terminalState.accountBookId().equals(accountBookId)) {
			return false;
		}
		sendProgress(taskId, emitter, terminalState.progress());
		flushTerminalEvent(taskId, emitter, terminalState);
		return true;
	}

	public void removeEmitter(String taskId) {
		emitters.remove(taskId);
	}

	public void publishProgress(String taskId, int progress) {
		ParsingTaskState state = activeTasks.get(taskId);
		if (state == null) {
			return;
		}

		int normalized = clamp(progress);
		activeTasks.put(
				taskId,
				new ParsingTaskState(state.accountBookId(), normalized, TaskStatus.ACTIVE, null));

		SseEmitter emitter = emitters.get(taskId);
		if (emitter != null) {
			sendProgress(taskId, emitter, normalized);
		}
	}

	public void complete(String taskId) {
		ParsingTaskState state = activeTasks.get(taskId);
		if (state == null) {
			return;
		}

		saveTerminalStateToRedis(
				taskId,
				new ParsingTaskState(state.accountBookId(), 100, TaskStatus.COMPLETE, null));

		activeTasks.remove(taskId);

		SseEmitter emitter = emitters.get(taskId);
		if (emitter != null) {
			flushTerminalEvent(
					taskId,
					emitter,
					new ParsingTaskState(state.accountBookId(), 100, TaskStatus.COMPLETE, null));
		}
	}

	public void publishError(String taskId, String errorMessage) {
		ParsingTaskState state = activeTasks.get(taskId);
		if (state == null) {
			return;
		}

		saveTerminalStateToRedis(
				taskId,
				new ParsingTaskState(
						state.accountBookId(), state.progress(), TaskStatus.ERROR, errorMessage));

		activeTasks.remove(taskId);

		SseEmitter emitter = emitters.get(taskId);
		if (emitter != null) {
			flushTerminalEvent(
					taskId,
					emitter,
					new ParsingTaskState(
							state.accountBookId(),
							state.progress(),
							TaskStatus.ERROR,
							errorMessage));
		}
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
		String errorMessage = hash.get(key, FIELD_ERROR_MESSAGE);

		Long parsedAccountBookId = Long.valueOf(accountBookId);
		int progress = parseProgress(progressValue);
		TaskStatus status = parseStatus(statusValue);
		return new ParsingTaskState(parsedAccountBookId, progress, status, errorMessage);
	}

	private void saveTerminalStateToRedis(String taskId, ParsingTaskState state) {
		HashOperations<String, String, String> hash = redisTemplate.opsForHash();
		String key = taskKey(taskId);
		hash.put(key, FIELD_ACCOUNT_BOOK_ID, String.valueOf(state.accountBookId()));
		hash.put(key, FIELD_PROGRESS, String.valueOf(clamp(state.progress())));
		hash.put(key, FIELD_STATUS, state.status().name());
		if (state.errorMessage() == null || state.errorMessage().isBlank()) {
			hash.delete(key, FIELD_ERROR_MESSAGE);
		} else {
			hash.put(key, FIELD_ERROR_MESSAGE, state.errorMessage());
		}
		applyCompletedTtl(key);
	}

	private void applyCompletedTtl(String key) {
		if (completedTaskTtlSeconds > 0) {
			redisTemplate.expire(key, completedTaskTtlSeconds, TimeUnit.SECONDS);
		}
	}

	private void sendProgress(String taskId, SseEmitter emitter, int progress) {
		boolean sent = sendEvent(emitter, taskId, "progress", new ParsingProgressEvent(progress));
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
								new ParsingErrorEvent(state.errorMessage()))
						: sendEvent(
								emitter,
								taskId,
								"complete",
								new ParsingProgressEvent(state.progress()));
		if (!sent) {
			emitters.remove(taskId);
			return;
		}

		try {
			if (state.status() == TaskStatus.ERROR) {
				emitter.completeWithError(new RuntimeException(state.errorMessage()));
			} else {
				emitter.complete();
			}
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

	private int clamp(int progress) {
		return Math.max(0, Math.min(100, progress));
	}
}
