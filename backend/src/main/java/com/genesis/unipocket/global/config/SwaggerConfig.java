package com.genesis.unipocket.global.config;

import com.genesis.unipocket.global.auth.annotation.LoginUser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		String schemeName = "access_token";

		return new OpenAPI()
				.info(
						new Info()
								.title("Unipocket API")
								.description(
										"Unipocket 백엔드 API 명세\n\n"
												+ "인증: HTTP-Only 쿠키(access_token) 기반.\n"
												+ "OAuth 로그인 후 쿠키가 자동 설정되므로,"
												+ " 별도 토큰 입력 없이 API 테스트가"
												+ " 가능합니다.")
								.version("v1.0.0"))
				.addSecurityItem(new SecurityRequirement().addList(schemeName))
				.components(
						new Components()
								.addSecuritySchemes(
										schemeName,
										new SecurityScheme()
												.type(SecurityScheme.Type.APIKEY)
												.in(SecurityScheme.In.COOKIE)
												.name(schemeName)));
	}

	@PostConstruct
	public void init() {
		// @LoginUser 어노테이션이 붙은 파라미터를 Swagger 문서에서 숨깁니다.
		SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginUser.class);
	}
}
