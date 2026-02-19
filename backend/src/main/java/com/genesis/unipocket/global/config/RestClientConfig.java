package com.genesis.unipocket.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
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
	@Value("${external.http.default-connect-timeout-seconds:5}")
	private int defaultConnectTimeoutSeconds;

	@Value("${external.http.default-read-timeout-seconds:10}")
	private int defaultReadTimeoutSeconds;

	@Value("${gemini.api.connect-timeout-seconds:10}")
	private int geminiConnectTimeoutSeconds;

	@Value("${gemini.api.read-timeout-seconds:90}")
	private int geminiReadTimeoutSeconds;

	@Bean
	public RestClient restClient() {
		return RestClient.builder().requestFactory(clientHttpRequestFactory()).build();
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
		return restTemplate;
	}

	@Bean(name = "geminiRestTemplate")
	public RestTemplate geminiRestTemplate() {
		return new RestTemplate(
				clientHttpRequestFactory(geminiConnectTimeoutSeconds, geminiReadTimeoutSeconds));
	}

	/**
	 * HTTP 요청 팩토리 설정
	 * - 연결 타임아웃: external.http.default-connect-timeout-seconds
	 * - 읽기 타임아웃: external.http.default-read-timeout-seconds
	 */
	private ClientHttpRequestFactory clientHttpRequestFactory() {
		return clientHttpRequestFactory(defaultConnectTimeoutSeconds, defaultReadTimeoutSeconds);
	}

	private ClientHttpRequestFactory clientHttpRequestFactory(
			int connectTimeoutSeconds, int readTimeoutSeconds) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
		factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
		return factory;
	}
}
