import {
  BASE_URL,
  DEFAULT_TIMEOUT,
  ERROR_NAMES,
  getDefaultErrorMessage,
  HTTP_STATUS,
} from './constants';
import { ApiError } from './error';

export const customFetch = async (
  endpoint: string,
  options: RequestInit = {},
) => {
  const { headers, ...restOptions } = options;

  // 1. 타임아웃 설정을 위한 AbortController
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), DEFAULT_TIMEOUT);

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      ...restOptions,
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        // 여기에 로컬스토리지 토큰 등을 공통으로 넣을 수 있습니다.
        ...headers,
      },
    });

    // 응답이 왔으므로 타이머 해제
    clearTimeout(timeoutId);

    // 2. HTTP 에러 처리 (400~500번대)
    if (!response.ok) {
      const errorBody = await response.json().catch(() => ({}));
      const message =
        errorBody.message || getDefaultErrorMessage(response.status);
      throw new ApiError({
        message,
        status: response.status,
      });
    }

    // 3. 정상 응답 처리 (200~299)
    if (response.status === HTTP_STATUS.NO_CONTENT) return null;
    return await response.json();
  } catch (error) {
    clearTimeout(timeoutId); // 에러 발생 시에도 타이머 해제 필수

    // 타임아웃 에러 처리 (AbortError)
    if (error instanceof Error && error.name === ERROR_NAMES.ABORT_ERROR) {
      throw new Error(
        '요청 시간이 초과되었습니다. 네트워크 상태를 확인해주세요.',
      );
    }

    // 이미 ApiError라면 그대로 던지고, 아니면
    // debugger;
    throw error;
  }
};
