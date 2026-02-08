package com.genesis.unipocket.global.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b> 기본 컨트롤러 </b>
 * <p>
 * 헬스체크 역할 수행
 */
@Tag(name = "헬스체크 기능")
@RestController
public class BaseController {

	@GetMapping("/health-check")
	public String healthCheck() {
		return "OK";
	}
}
