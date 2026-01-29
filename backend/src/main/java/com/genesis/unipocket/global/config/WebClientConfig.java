package com.genesis.unipocket.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient 설정
 * OAuth2 Provider (Google, Kakao)와 HTTP 통신을 위한 WebClient Bean 생성
 */
@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient() {
		// Netty 기반의 HttpClient 설정 (타임아웃 및 리소스 최적화)
		HttpClient httpClient =
				HttpClient.create()
						.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 연결 타임아웃 5초
						.responseTimeout(Duration.ofSeconds(10)) // 전체 응답 타임아웃 10초
						.doOnConnected(
								conn ->
										conn.addHandlerLast(
														new ReadTimeoutHandler(
																10,
																TimeUnit.SECONDS)) // 데이터 읽기 타임아웃
												// 10초
												.addHandlerLast(
														new WriteTimeoutHandler(
																10,
																TimeUnit.SECONDS))); // 데이터 쓰기 타임아웃
		// 10초

		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient)) // Reactor Netty 커넥터 적용
				.defaultHeader(
						"Content-Type", "application/x-www-form-urlencoded") // OAuth2 요청 기본 헤더
				.build();
	}
}
