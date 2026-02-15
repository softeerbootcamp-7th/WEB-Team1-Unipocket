package com.genesis.unipocket.global.observability.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ObservabilityAspect {

	private final MeterRegistry meterRegistry;
	private final ObjectMapper objectMapper;
	private final Map<MetricTimerKey, Timer> timerCache = new ConcurrentHashMap<>();

	@Pointcut("within(@org.springframework.stereotype.Service *)")
	public void servicePointcut() {}

	@Pointcut(
			"within(@org.springframework.stereotype.Controller *) ||"
					+ " within(@org.springframework.web.bind.annotation.RestController *)")
	public void controllerPointcut() {}

	@Pointcut("within(@org.springframework.stereotype.Repository *)")
	public void repositoryPointcut() {}

	@Pointcut(
			"execution(* com.genesis.unipocket..*Worker.*(..)) || execution(*"
					+ " com.genesis.unipocket..*Scheduler.*(..))")
	public void workerPointcut() {}

	@Pointcut("execution(* com.genesis.unipocket..*Client.*(..))")
	public void externalApiPointcut() {}

	@Pointcut("@annotation(com.genesis.unipocket.global.observability.annotation.TrackTime)")
	public void trackTimePointcut() {}

	@Around(
			"servicePointcut() || controllerPointcut() || repositoryPointcut() || workerPointcut()"
					+ " || externalApiPointcut() || trackTimePointcut()")
	public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.nanoTime();
		boolean success = true;
		Object result = null;
		Throwable exception = null;

		try {
			result = joinPoint.proceed();
			return result;
		} catch (Throwable e) {
			success = false;
			exception = e;
			throw e;
		} finally {
			long duration = System.nanoTime() - startTime;
			recordMetricsAndLog(joinPoint, duration, success, exception);
		}
	}

	private void recordMetricsAndLog(
			ProceedingJoinPoint joinPoint,
			long durationNanos,
			boolean success,
			Throwable exception) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String className = signature.getDeclaringType().getSimpleName();
		String methodName = signature.getName();

		Map<String, Object> logData = new HashMap<>();
		logData.put("className", className);
		logData.put("methodName", methodName);
		logData.put("durationMs", TimeUnit.NANOSECONDS.toMillis(durationNanos));
		logData.put("success", success);

		String userId = getUserId();
		if (userId != null) {
			logData.put("userId", userId);
		}

		String jobId = MDC.get("jobId"); // Assuming jobId is set in MDC by some filter/interceptor
		if (jobId != null) {
			logData.put("jobId", jobId);
		}

		String endpoint = getEndpoint();
		if (endpoint != null) {
			logData.put("endpoint", endpoint);
		}

		if (!success && exception != null) {
			logData.put("exception", exception.getClass().getSimpleName());
			logData.put("exceptionMessage", exception.getMessage());
		}

		try {
			log.info(objectMapper.writeValueAsString(logData));
		} catch (Exception e) {
			log.error("Failed to log observability data", e);
		}

		String exceptionName = exception != null ? exception.getClass().getSimpleName() : "none";
		MetricTimerKey metricTimerKey =
				new MetricTimerKey(className, methodName, String.valueOf(success), exceptionName);
		Timer timer =
				timerCache.computeIfAbsent(
						metricTimerKey,
						key ->
								Timer.builder("method.execution.time")
										.tag("class", key.className())
										.tag("method", key.methodName())
										.tag("success", key.success())
										.tag("exception", key.exceptionName())
										.register(meterRegistry));
		timer.record(durationNanos, TimeUnit.NANOSECONDS);
	}

	private String getUserId() {
		// Try extracting from SecurityContext via utility or MDC if available
		// For now, let's try MDC "userId" if set by AuthenticationFilter
		return MDC.get("userId");
	}

	private String getEndpoint() {
		try {
			ServletRequestAttributes attributes =
					(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				return attributes.getRequest().getRequestURI();
			}
		} catch (Exception e) {
			log.trace("Could not get endpoint, not in a request context.", e);
		}
		return null;
	}

	private record MetricTimerKey(
			String className, String methodName, String success, String exceptionName) {}
}
