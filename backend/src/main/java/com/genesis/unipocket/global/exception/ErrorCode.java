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

	// 인증/인가 에러
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401_UNAUTHORIZED", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "403_FORBIDDEN", "접근 권한이 없습니다."),

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

	// ========== User Errors ==========
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "404_USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "409_USER_ALREADY_EXISTS", "이미 존재하는 사용자입니다."),

	// ========== User Card Errors ==========
	CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "404_CARD_NOT_FOUND", "카드를 찾을 수 없습니다."),
	CARD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "400_CARD_LIMIT_EXCEEDED", "카드 등록 한도를 초과했습니다."),
	CARD_NOT_OWNED(HttpStatus.FORBIDDEN, "403_CARD_NOT_OWNED", "본인의 카드가 아닙니다."),

	// ========== Token Errors ==========
	TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "401_TOKEN_REQUIRED", "로그인이 필요합니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "401_TOKEN_EXPIRED", "토큰이 만료되었습니다."),
	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "401_TOKEN_INVALID", "유효하지 않은 토큰입니다."),
	TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "401_TOKEN_BLACKLISTED", "로그아웃된 토큰입니다."),
	REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "400_REFRESH_TOKEN_REQUIRED", "리프레시 토큰이 필요합니다."),

	// Account Book
	ACCOUNT_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "404_ACCOUNT_BOOK_NOT_FOUND", "가계부를 찾을 수 없습니다."),
	ACCOUNT_BOOK_CREATE_VALIDATION_FAILED(
			HttpStatus.BAD_REQUEST,
			CodeLiterals.ACCOUNT_BOOK_CREATE_VALIDATION_FAILED,
			"입력값이 올바르지 않습니다."),
	ACCOUNT_BOOK_UPDATE_VALIDATION_FAILED(
			HttpStatus.BAD_REQUEST,
			CodeLiterals.ACCOUNT_BOOK_UPDATE_VALIDATION_FAILED,
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
	ACCOUNT_BOOK_BUDGET_NOT_SET(
			HttpStatus.BAD_REQUEST, "400_ACCOUNT_BOOK_BUDGET_NOT_SET", "해당 가계부에 예산이 설정되어 있지 않습니다."),
	ACCOUNT_BOOK_UNAUTHORIZED_ACCESS(
			HttpStatus.FORBIDDEN, "403_ACCOUNT_BOOK_UNAUTHORIZED_ACCESS", "해당 가계부에 접근할 권한이 없습니다."),

	// Travel Errors
	TRAVEL_INVALID_DATE_RANGE(
			HttpStatus.BAD_REQUEST, "400_TRAVEL_INVALID_DATE_RANGE", "여행 종료 날짜는 시작 날짜보다 이후여야 합니다."),
	TRAVEL_NOT_FOUND(HttpStatus.NOT_FOUND, "404_TRAVEL_NOT_FOUND", "여행 정보를 찾을 수 없습니다."),

	// Expense Errors
	EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "404_EXPENSE_NOT_FOUND", "지출 내역을 찾을 수 없습니다."),
	EXPENSE_UNAUTHORIZED_ACCESS(
			HttpStatus.FORBIDDEN, "403_EXPENSE_UNAUTHORIZED_ACCESS", "해당 지출 내역에 접근할 권한이 없습니다."),
	EXPENSE_INVALID_CURRENCY(
			HttpStatus.BAD_REQUEST, "400_EXPENSE_INVALID_CURRENCY", "유효하지 않은 통화 코드입니다."),
	EXPENSE_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "400_EXPENSE_INVALID_AMOUNT", "금액은 0보다 커야 합니다."),
	EXPENSE_INVALID_SORT(
			HttpStatus.BAD_REQUEST,
			"400_EXPENSE_INVALID_SORT",
			"유효하지 않은 정렬 기준입니다. ( occurredAt,desc(asc) baseCurrencyAmount,desc(asc) )"),

	// 환율 관련 에러
	EXCHANGE_RATE_API_ERROR(
			HttpStatus.SERVICE_UNAVAILABLE,
			"503_EXCHANGE_RATE_API_ERROR",
			"환율 정보를 가져올 수 없습니다. 잠시 후 다시 시도해주세요."),
	EXCHANGE_RATE_NOT_FOUND(
			HttpStatus.NOT_FOUND, "404_EXCHANGE_RATE_NOT_FOUND", "해당 통화의 환율 정보를 찾을 수 없습니다."),

	// Temp Expense Errors
	TEMP_EXPENSE_NOT_FOUND(
			HttpStatus.NOT_FOUND, "404_TEMP_EXPENSE_NOT_FOUND", "임시 지출 내역을 찾을 수 없습니다."),
	TEMP_EXPENSE_META_NOT_FOUND(
			HttpStatus.NOT_FOUND, "404_TEMP_EXPENSE_META_NOT_FOUND", "임시 지출 메타데이터를 찾을 수 없습니다."),
	TEMP_EXPENSE_FILE_NOT_FOUND(
			HttpStatus.NOT_FOUND, "404_TEMP_EXPENSE_FILE_NOT_FOUND", "임시 지출 파일을 찾을 수 없습니다."),
	TEMP_EXPENSE_SCOPE_MISMATCH(
			HttpStatus.BAD_REQUEST,
			"400_TEMP_EXPENSE_SCOPE_MISMATCH",
			"요청한 가계부와 임시 지출 리소스의 소속이 일치하지 않습니다."),
	TEMP_EXPENSE_PARSE_FILES_REQUIRED(
			HttpStatus.BAD_REQUEST,
			"400_TEMP_EXPENSE_PARSE_FILES_REQUIRED",
			"파싱할 파일이 선택되지 않았습니다."),
	TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED(
			HttpStatus.BAD_REQUEST,
			"400_TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED",
			"파일 업로드 개수 제한을 초과했습니다."),
	TEMP_EXPENSE_INVALID_FILE_TYPE(
			HttpStatus.BAD_REQUEST, "400_TEMP_EXPENSE_INVALID_FILE_TYPE", "지원하지 않는 파일 타입입니다."),
	TEMP_EXPENSE_PARSE_FAILED(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"500_TEMP_EXPENSE_PARSE_FAILED",
			"임시 지출 파싱 처리 중 오류가 발생했습니다."),

	// ========== Widget Errors ==========
	WIDGET_ORDER_DUPLICATED(
			HttpStatus.BAD_REQUEST, "400_WIDGET_ORDER_DUPLICATED", "위젯 순서가 중복되었습니다."),
	WIDGET_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "400_WIDGET_SIZE_EXCEEDED", "위젯 크기 합이 5를 초과합니다.");

	public static class CodeLiterals {
		public static final String ACCOUNT_BOOK_CREATE_VALIDATION_FAILED =
				"400_ACCOUNT_BOOK_CREATE_VALIDATION_FAILED";
		public static final String ACCOUNT_BOOK_UPDATE_VALIDATION_FAILED =
				"400_ACCOUNT_BOOK_UPDATE_VALIDATION_FAILED";
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
