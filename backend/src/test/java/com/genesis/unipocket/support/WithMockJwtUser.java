package com.genesis.unipocket.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 통합 테스트에서 인증된 사용자를 시뮬레이션하는 어노테이션
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMockJwtUser {

	String userId() default "00000000-0000-0000-0000-000000000001";
}
