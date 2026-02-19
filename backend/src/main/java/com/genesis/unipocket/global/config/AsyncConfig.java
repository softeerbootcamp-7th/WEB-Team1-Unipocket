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
}
