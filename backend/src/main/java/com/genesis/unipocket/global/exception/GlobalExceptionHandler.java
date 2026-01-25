package com.genesis.unipocket.global.exception;

import com.genesis.unipocket.global.common.dto.ErrorResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
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
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * 비즈니스 로직 실행 중 발생하는 커스텀 예외 처리
	 */
	@ExceptionHandler(GlobalException.class)
	protected ResponseEntity<ErrorResponse> handleServiceException(GlobalException e) {
		logException(e.getCode(), e);
		return createErrorResponse(e.getCode());
	}

	/**
	 * &#064;Valid  검증 실패 시 발생 (400)
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			@NonNull MethodArgumentNotValidException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {

		logException(ErrorCode.INVALID_INPUT_VALUE, e);

		return handleExceptionInternal(e, ErrorCode.INVALID_INPUT_VALUE, headers, status, request);
	}

	/**
	 * 지원하지 않는 HTTP 메소드 호출 시 발생 (405)
	 */
	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
			@NonNull HttpRequestMethodNotSupportedException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {

		logException(ErrorCode.METHOD_NOT_ALLOWED, e);

		return handleExceptionInternal(e, ErrorCode.METHOD_NOT_ALLOWED, headers, status, request);
	}

	/**
	 * 없는 리소스(URL) 요청 시 발생 (404)
	 */
	@Override
	protected ResponseEntity<Object> handleNoResourceFoundException(
			@NonNull NoResourceFoundException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {

		logException(ErrorCode.RESOURCE_NOT_FOUND, e);

		return ResponseEntity.status(status).body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
	}

	/**
	 * 지원하지 않는 미디어 타입(Content-Type) 요청 시 발생 (415)
	 */
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
			@NonNull HttpMediaTypeNotSupportedException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {

		logException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, e);

		return handleExceptionInternal(
				e, ErrorCode.UNSUPPORTED_MEDIA_TYPE, headers, status, request);
	}

	/**
	 * 그 외 정의되지 않은 모든 시스템 예외 처리 (500)
	 */
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception e) {
		logException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<Object> handleExceptionInternal(
			Exception e,
			ErrorCode errorCode,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {

		return super.handleExceptionInternal(
				e, ErrorResponse.of(errorCode), headers, status, request);
	}

	private void logException(ErrorCode errorCode, Exception e) {
		if (errorCode.getStatus().is5xxServerError()) {
			log.error(
					"Server Error: [{} - {}] {}",
					errorCode.getCode(),
					errorCode.getMessage(),
					e.getMessage(),
					e);
		} else {
			log.warn(
					"Client Error: [{} - {}] {}",
					errorCode.getCode(),
					errorCode.getMessage(),
					e.getMessage());
		}
	}

	private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode));
	}
}
