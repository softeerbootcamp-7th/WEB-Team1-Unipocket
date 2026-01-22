package com.genesis.unipocket.global.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <b> 기본 컨트롤러 </b>
 * <p>
 * 헬스체크 역할 수행
 */
@Controller
public class BaseController {

    @GetMapping("/health-check")
    @ResponseBody
    public String healthCheck() {
        return "OK";
    }
}
