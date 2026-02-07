package com.genesis.unipocket.global.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * RestClient 및 RestTemplate 설정
 * OAuth2 Provider (Google, Kakao)와 HTTP 통신을 위한 RestClient Bean 생성
 * 환율 API 등 외부 API 통신을 위한 RestTemplate Bean 생성
 */
@Configuration
public class RestClientConfig {
	// 타임아웃 설정을 변수로 추출
	private static final int CONNECT_TIMEOUT_SECONDS = 5;
	private static final int READ_TIMEOUT_SECONDS = 10;

	@Bean
	public RestClient restClient() {
		return RestClient.builder().requestFactory(clientHttpRequestFactory()).build();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate(clientHttpRequestFactory());
	}

	/**
	 * HTTP 요청 팩토리 설정
	 * - 연결 타임아웃: 5초
	 * - 읽기 타임아웃: 10초
	 */
	private ClientHttpRequestFactory clientHttpRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS)); // 연결 타임아웃
		factory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS)); // 읽기 타임아웃
		return factory;
	}
}
