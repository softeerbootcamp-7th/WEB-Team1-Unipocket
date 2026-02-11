package com.genesis.unipocket.global.config;

import com.genesis.unipocket.auth.resolver.LoginUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final LoginUserArgumentResolver loginUserArgumentResolver;

	// AuthController와 동일하게 SpEL을 사용하여 주입 시점에 정규화(소문자, trim) 완료
	@Value("#{'${app.frontend.allowed-origins:}'.split(',').![#this.trim().toLowerCase()]}")
	private List<String> allowedOriginList;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(loginUserArgumentResolver);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		if (allowedOriginList.isEmpty()) {
			return;
		}

		registry.addMapping("/**")
				.allowedOrigins(allowedOriginList.toArray(String[]::new))
				.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true)
				.maxAge(3600); // 브라우저에 CORS 결과 캐싱 시간 추가 (성능 향상)
	}
}
