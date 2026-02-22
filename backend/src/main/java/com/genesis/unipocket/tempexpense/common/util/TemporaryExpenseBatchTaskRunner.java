package com.genesis.unipocket.tempexpense.common.util;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import java.util.List;

public final class TemporaryExpenseBatchTaskRunner {

	private TemporaryExpenseBatchTaskRunner() {}

	public static <T> void run(
			String taskId,
			List<T> items,
			ParsingProgressPublisher progressPublisher,
			ErrorCode fallbackErrorCode,
			ItemProcessor<T> itemProcessor,
			ItemErrorHandler<T> itemErrorHandler) {
		int total = items.size();
		int completed = 0;
		progressPublisher.publishProgress(taskId, 0);

		try {
			for (T item : items) {
				try {
					itemProcessor.process(item);
				} catch (Exception e) {
					if (itemErrorHandler.shouldAbort(item, e)) {
						throw e;
					}
				} finally {
					completed++;
					progressPublisher.publishProgress(
							taskId, TemporaryExpenseTaskSupport.toPercent(completed, total));
				}
			}
			progressPublisher.complete(taskId);
		} catch (Exception e) {
			String errorMessage =
					TemporaryExpenseTaskSupport.resolveClientErrorMessage(e, fallbackErrorCode);
			progressPublisher.publishError(taskId, errorMessage);
			throw TemporaryExpenseTaskSupport.rethrow(e);
		}
	}

	public interface ItemProcessor<T> {
		void process(T item);
	}

	public interface ItemErrorHandler<T> {
		boolean shouldAbort(T item, Exception e);
	}
}
