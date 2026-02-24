package com.genesis.unipocket.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <b>비동기 처리 설정</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	/**
	 * 파일 파싱용 Executor
	 */
	@Bean(name = "parsingExecutor")
	public Executor parsingExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("parsing-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		executor.initialize();
		return executor;
	}

	@Bean(name = "analysisBatchExecutor")
	public Executor analysisBatchExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
		executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("analysis-batch-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		executor.initialize();
		return executor;
	}

	/**
	 * 개별 파일 파싱용 Executor — 배치 내 파일별 병렬 처리에 사용.
	 * parsingExecutor(배치 레벨)와 분리하여 스레드 풀 기아(starvation)를 방지한다.
	 */
	@Bean(name = "fileParsingExecutor")
	public Executor fileParsingExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(6); // 2 배치 × 3 파일 동시 처리
		executor.setMaxPoolSize(15); // 최대 5 배치 × 3 파일 동시 처리
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("file-parse-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		executor.initialize();
		return executor;
	}
}
