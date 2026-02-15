package com.genesis.unipocket.global.observability.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ObservabilityAspectTest {

	private ObservabilityAspect aspect;
	private MeterRegistry meterRegistry;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		meterRegistry = new SimpleMeterRegistry();
		objectMapper = mock(ObjectMapper.class);
		aspect = new ObservabilityAspect(meterRegistry, objectMapper);
	}

	@Test
	@DisplayName("메서드 실행 시간을 측정하고 로그와 메트릭을 남긴다")
	void measuresExecutionTime() throws Throwable {
		// given
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		MethodSignature signature = mock(MethodSignature.class);

		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getDeclaringType()).thenReturn(this.getClass());
		when(signature.getName()).thenReturn("testMethod");
		when(joinPoint.proceed()).thenReturn("result");

		// when
		Object result = aspect.measureExecutionTime(joinPoint);

		// then
		assertThat(result).isEqualTo("result");

		// Verify Metric
		assertThat(meterRegistry.find("method.execution.time").timer()).isNotNull();
		assertThat(meterRegistry.find("method.execution.time").timer().count()).isEqualTo(1);

		// Verify Log (ObjectMapper called)
		verify(objectMapper).writeValueAsString(any());
	}
}
