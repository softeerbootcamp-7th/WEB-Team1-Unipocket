package com.genesis.unipocket.global.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b> 기본 컨트롤러 </b>
 * <p>
 * 헬스체크 역할 수행
 */
@RestController
public class BaseController {

	@GetMapping("/health-check")
	public String healthCheck() {
		return "OK";
	}
}
