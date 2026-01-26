package com.genesis.unipocket.global.exception;

import com.genesis.unipocket.global.common.dto.CustomErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * <b>공용 예외 핸들러</b>
 * <p>
 * 비즈니스 코드에서 처리되지 못한 에러들을 처리하기 위한 전역 핸들러 입니다. <br>
 * 에러 디버깅을 위한 로깅과 에러 코드 반환을 통해 서버측에 로그를 남기고 <br>
 * 클라이언트 측에 일관성 있는 에러 객체를 전달합니다.
 * </p>
 *
 * @author bluefishez
 * @since 2026-01-26
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 로직 실행 중 발생하는 커스텀 예외 처리
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<CustomErrorResponse> handleBusinessException(
			HttpServletRequest req, BusinessException e) {

		return createErrorResponse(e.getCode());
	}

	/**
	 * &#064;Valid  검증 실패 시 발생 (400)
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CustomErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException e) {

		return createErrorResponse(ErrorCode.INVALID_INPUT_VALUE);
	}

	/**
	 * 지원하지 않는 HTTP 메소드 호출 시 발생 (405)
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<CustomErrorResponse> handleHttpRequestMethodNotSupported(
			HttpRequestMethodNotSupportedException e) {

		return createErrorResponse(ErrorCode.METHOD_NOT_ALLOWED);
	}

	/**
	 * 없는 리소스(URL) 요청 시 발생 (404)
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<CustomErrorResponse> handleNoResourceFoundException(
			NoResourceFoundException e) {

		return createErrorResponse(ErrorCode.RESOURCE_NOT_FOUND);
	}

	/**
	 * 지원하지 않는 미디어 타입(Content-Type) 요청 시 발생 (415)
	 */
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<CustomErrorResponse> handleHttpMediaTypeNotSupported(
			HttpMediaTypeNotSupportedException e) {

		return createErrorResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
	}

	/**
	 * 그 외 정의되지 않은 모든 시스템 예외 처리 (500)
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<CustomErrorResponse> handleException(
			HttpServletRequest req, Exception e) {

		logException(ErrorCode.INTERNAL_SERVER_ERROR, req, e);
		return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	private void logException(ErrorCode errorCode, HttpServletRequest req, Exception e) {
		if (!errorCode.getStatus().is5xxServerError()) {
			return;
		}

		String clientIp = req.getRemoteAddr();
		String queryString = req.getQueryString() != null ? "?" + req.getQueryString() : "";
		String userAgent = req.getHeader("User-Agent");

		log.error(
				"[EXCEPTION] {} {}{} | IP: {} | UA: {} | Code: {} | Message: {}",
				req.getMethod(),
				req.getRequestURI(),
				queryString,
				clientIp,
				userAgent != null ? userAgent : "None",
				errorCode.getCode(),
				errorCode.getMessage(),
				e);
	}

	private ResponseEntity<CustomErrorResponse> createErrorResponse(ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getStatus()).body(CustomErrorResponse.of(errorCode));
	}
}
