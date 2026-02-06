export const DEFAULT_TIMEOUT = 5000; // 5초

export const ERROR_NAMES = {
  ABORT_ERROR: 'AbortError',
  API_ERROR: 'ApiError',
  NETWORK_ERROR: 'TypeError', // fetch가 네트워크 단절 시 던지는 기본 에러명
} as const;

export type ErrorName = (typeof ERROR_NAMES)[keyof typeof ERROR_NAMES];

export const HTTP_STATUS = {
  OK: 200,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500,
} as const;

export type HttpStatus = (typeof HTTP_STATUS)[keyof typeof HTTP_STATUS];

const DEFAULT_ERROR_MESSAGES: Record<number, string> = {
  [HTTP_STATUS.UNAUTHORIZED]: '로그인이 필요하거나 세션이 만료되었습니다.',
  [HTTP_STATUS.FORBIDDEN]: '접근 권한이 없습니다.',
  [HTTP_STATUS.INTERNAL_SERVER_ERROR]:
    '서버 점검 중입니다. 잠시 후 다시 시도해주세요.',
};

export function getDefaultErrorMessage(status: number): string {
  return DEFAULT_ERROR_MESSAGES[status] || '알 수 없는 에러';
}
