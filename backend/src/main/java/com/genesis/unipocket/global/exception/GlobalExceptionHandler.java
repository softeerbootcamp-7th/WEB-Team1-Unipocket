package com.genesis.unipocket.global.exception;

import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.global.common.dto.CustomErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
	 * 요청 제한 초과 예외 처리 (429 + Retry-After)
	 */
	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<CustomErrorResponse> handleRateLimitExceededException(
			RateLimitExceededException e) {
		return ResponseEntity.status(e.getCode().getStatus())
				.header("Retry-After", String.valueOf(e.getRetryAfterSeconds()))
				.body(CustomErrorResponse.of(e.getCode()));
	}

	/**
	 * &#064;Valid 검증 실패 시 발생 (400)
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CustomErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException e) {

		String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		if (message == null) {
			message = ErrorCode.INVALID_INPUT_VALUE.getMessage();
		}

		return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
				.body(new CustomErrorResponse(ErrorCode.INVALID_INPUT_VALUE.getCode(), message));
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
	public ResponseEntity<?> handleNoResourceFoundException(
			HttpServletRequest req, NoResourceFoundException e) {
		// 브라우저의 정적 리소스 요청(예: text/css, image/*)에서는 JSON 직렬화 협상 실패를 피하기 위해
		// 바디 없이 404만 반환한다.
		if (!acceptsJson(req)) {
			return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getStatus()).build();
		}
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
	 * Accept 협상 실패 시 발생 (406)
	 */
	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ResponseEntity<Void> handleHttpMediaTypeNotAcceptable(
			HttpMediaTypeNotAcceptableException e) {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}

	/**
	 * 필수 요청 파라미터 누락 시 발생 (400)
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<CustomErrorResponse> handleMissingServletRequestParameter(
			MissingServletRequestParameterException e) {

		String message = String.format("필수 요청 파라미터(%s)가 누락되었습니다.", e.getParameterName());
		return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
				.body(new CustomErrorResponse(ErrorCode.INVALID_INPUT_VALUE.getCode(), message));
	}

	/**
	 * 경로 변수/쿼리 파라미터 타입 변환 실패 시 발생 (400)
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<CustomErrorResponse> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException e) {

		String message = String.format("요청 파라미터(%s) 타입이 올바르지 않습니다.", e.getName());
		return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
				.body(new CustomErrorResponse(ErrorCode.INVALID_INPUT_VALUE.getCode(), message));
	}

	/**
	 * 필수 쿠키 누락 시 발생 (400/401)
	 */
	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<CustomErrorResponse> handleMissingRequestCookie(
			MissingRequestCookieException e) {
		ErrorCode errorCode =
				AuthCookieConstants.REFRESH_TOKEN.equals(e.getCookieName())
						? ErrorCode.REFRESH_TOKEN_REQUIRED
						: ErrorCode.TOKEN_REQUIRED;
		return createErrorResponse(errorCode);
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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleHttpMessageNotReadableException(
			HttpMessageNotReadableException e) {
		return createErrorResponse(ErrorCode.HTTP_MESSAGE_NOT_READABLE);
	}

	/**
	 * 비동기 요청 타임아웃(SSE 포함)은 정상적인 종료 케이스로 간주
	 */
	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public ResponseEntity<Void> handleAsyncRequestTimeoutException(
			HttpServletRequest req, AsyncRequestTimeoutException e) {
		log.debug("Async request timed out: {} {}", req.getMethod(), req.getRequestURI());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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

	private boolean acceptsJson(HttpServletRequest req) {
		String accept = req.getHeader("Accept");
		if (accept == null || accept.isBlank()) {
			return true;
		}
		return accept.contains(MediaType.APPLICATION_JSON_VALUE) || accept.contains("*/*");
	}
}
