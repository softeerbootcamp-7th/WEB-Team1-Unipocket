package com.genesis.unipocket.global.exception;

/**
 * <b>전체 비즈니스 에러 코드</b>
 * <p>
 * 커스텀 예외 정의를 해둔 enum 이며, ErrorResponse 정의에 사용됩니다.
 * <br>
 * 적절한 HTTP 응답 코드, 프론트 엔드에게 전달하기 위한 에러 코드를 위한 별도의 코드네임과 전달 메세지를 담습니다.
 * </p>
 * @author bluefishez
 * @since 2026-01-25
 */
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {
	// 서버 에러
	INTERNAL_SERVER_ERROR(
			HttpStatus.INTERNAL_SERVER_ERROR, "500_INTERNAL_SERVER_ERROR", "서버 내부 에러가 발생했습니다."),

	// 요청 에러
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "400_INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다."),
	TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "400_TYPE_MISMATCH", "파라미터 타입이 일치하지 않습니다."),
	HTTP_MESSAGE_NOT_READABLE(
			HttpStatus.BAD_REQUEST,
			"400_HTTP_MESSAGE_NOT_READABLE",
			"요청 본문을 읽을 수 없습니다. (JSON 형식을 확인하세요)"),

	// 리소스 에러
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "404_RESOURCE_NOT_FOUND", "리소스를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(
			HttpStatus.METHOD_NOT_ALLOWED, "405_METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메소드입니다."),

	UNSUPPORTED_MEDIA_TYPE(
			HttpStatus.UNSUPPORTED_MEDIA_TYPE, "415_UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 미디어 타입입니다."),

	// OAuth 에러
	INVALID_OAUTH_PROVIDER(
			HttpStatus.BAD_REQUEST, "400_INVALID_OAUTH_PROVIDER", "지원하지 않는 OAuth Provider입니다."),
	OAUTH_PROVIDER_NOT_CONFIGURED(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"500_OAUTH_PROVIDER_NOT_CONFIGURED",
			"OAuth Provider 설정이 올바르지 않습니다."),
	OAUTH_AUTHENTICATION_FAILED(
			HttpStatus.UNAUTHORIZED, "401_OAUTH_AUTHENTICATION_FAILED", "OAuth 인증에 실패했습니다."),
	OAUTH_TOKEN_REQUEST_FAILED(
			HttpStatus.BAD_REQUEST, "400_OAUTH_TOKEN_REQUEST_FAILED", "OAuth 토큰 요청에 실패했습니다."),
	OAUTH_USERINFO_REQUEST_FAILED(
			HttpStatus.BAD_REQUEST,
			"400_OAUTH_USERINFO_REQUEST_FAILED",
			"OAuth 사용자 정보 조회에 실패했습니다."),
	INVALID_OAUTH_STATE(
			HttpStatus.BAD_REQUEST, "400_INVALID_OAUTH_STATE", "OAuth State 값이 유효하지 않습니다."),
	OAUTH_STATE_EXPIRED(HttpStatus.BAD_REQUEST, "400_OAUTH_STATE_EXPIRED", "OAuth State가 만료되었습니다."),

	// ========== User Errors (3000~3999) ==========
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),

	// ========== Token Errors (4000~4999) ==========
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "T001", "토큰이 만료되었습니다."),
	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "T002", "유효하지 않은 토큰입니다.");
  
	// Account Book
	ACCOUNT_BOOK_CREATE_VALIDATION_FAILED(
			HttpStatus.BAD_REQUEST,
			CodeLiterals.ACCOUNT_BOOK_CREATE_VALIDATION_FAILED,
			"입력값이 올바르지 않습니다."),
	ACCOUNT_BOOK_INVALID_DATE_RANGE(
			HttpStatus.BAD_REQUEST,
			"400_ACCOUNT_BOOK_INVALID_DATE_RANGE",
			"시작 날짜는 종료 날짜보다 이전이어야 합니다."),
	ACCOUNT_BOOK_INVALID_COUNTRY_CODE(
			HttpStatus.BAD_REQUEST,
			"400_ACCOUNT_BOOK_INVALID_COUNTRY_CODE",
			"현지 통화와 기준 통화는 달라야 합니다."),
	ACCOUNT_BOOK_INVALID_BUDGET(
			HttpStatus.BAD_REQUEST, "400_ACCOUNT_BOOK_INVALID_BUDGET", "예산은 0 이상이어야 합니다."),
	ACCOUNT_BOOK_UNAUTHORIZED_ACCESS(
			HttpStatus.FORBIDDEN, "403_ACCOUNT_BOOK_UNAUTHORIZED_ACCESS", "해당 가계부에 접근할 권한이 없습니다.");

	public static class CodeLiterals {
		public static final String ACCOUNT_BOOK_CREATE_VALIDATION_FAILED =
				"400_ACCOUNT_BOOK_CREATE_VALIDATION_FAILED";
	}

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
