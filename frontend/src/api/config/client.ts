import {
  API_BASE_URL,
  DEFAULT_TIMEOUT,
  ERROR_NAMES,
  getDefaultErrorMessage,
  HTTP_STATUS,
} from './constants';
import { ApiError } from './error';

interface customFetchParams {
  endpoint: string;
  options?: RequestInit;
  isRetry?: boolean; // 재시도 여부 플래그 -> 401 재발급 시도 무한 루프 방지용
}

// 401 발생 시 토큰 재발급 요청을 처리하는 함수
const refreshAccessToken = async () => {
  // customFetch를 사용하되 isRetry=true로 설정하여 계속 401이 응답으로 오면 무한 루프 방지
  await customFetch({
    endpoint: '/auth/reissue',
    options: { method: 'POST' },
    isRetry: true,
  });
};

export const customFetch = async <T>({
  endpoint,
  options = {},
  isRetry = false,
}: customFetchParams): Promise<T> => {
  const { headers, ...restOptions } = options;

  // 타임아웃 설정을 위한 AbortController
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), DEFAULT_TIMEOUT);

  // URL 안전하게 결합 (슬래시 중복 방지)
  const url = `${API_BASE_URL.replace(/\/$/, '')}/${endpoint.replace(/^\//, '')}`;

  try {
    const response = await fetch(url, {
      ...restOptions,
      signal: controller.signal,
      headers: {
        ...headers,
        'Content-Type': 'application/json',
      },
      credentials: 'include', // 쿠키 포함
    });

    // 응답이 왔으므로 타이머 해제
    clearTimeout(timeoutId);

    // accessToken 만료로 401 응답이 온 경우
    // 재시도 중이 아니라면 토큰 재발급 시도
    if (response.status === HTTP_STATUS.UNAUTHORIZED && !isRetry) {
      try {
        // 토큰 재발급 시도 (조용히 처리, 성공 시 로그 없음)
        console.log('토큰 만료로 인한 재발급 시도...');
        await refreshAccessToken();

        // 재발급 성공 시 원래 요청 재시도 (isRetry=true로 설정)
        return await customFetch<T>({ endpoint, options, isRetry: true });
      } catch (error) {
        // 재발급 실패 시에만 로그 출력
        console.error('토큰 재발급 실패:', error);
        throw new ApiError({
          message: '세션이 만료되었습니다. 다시 로그인해주세요.',
          status: HTTP_STATUS.UNAUTHORIZED,
        });
      }
    }

    // 401 재발급 재시도 중에도 401이 발생하면 바로 에러
    if (response.status === HTTP_STATUS.UNAUTHORIZED && isRetry) {
      throw new ApiError({
        message: '세션이 만료되었습니다. 다시 로그인해주세요.',
        status: HTTP_STATUS.UNAUTHORIZED,
      });
    }

    // HTTP 에러 처리 (400~500번대)
    if (!response.ok) {
      // JSON 파싱이 실패하더라도 에러를 내지 않고 null을 반환하게 만든 안전장치
      const status = response.status;
      const errorBody = await response.json().catch(() => null);
      const message = errorBody?.message || getDefaultErrorMessage(status);

      throw new ApiError({
        message,
        status,
      });
    }

    return await response.json();
  } catch (error) {
    clearTimeout(timeoutId); // 에러 발생 시에도 타이머 해제 필수

    // 타임아웃 에러 처리 (AbortError)
    if (error instanceof Error && error.name === ERROR_NAMES.ABORT_ERROR) {
      throw new ApiError({
        message: '요청 시간이 초과되었습니다. 네트워크 상태를 확인해주세요.',
        status: HTTP_STATUS.REQUEST_TIMEOUT,
      });
    }

    // 이미 ApiError라면 그대로 던지고
    if (error instanceof ApiError) {
      throw error;
    }

    // 그 외는 네트워크 에러로 간주
    throw new ApiError({
      message: '네트워크 오류가 발생했습니다. 다시 시도해주세요.',
      status: HTTP_STATUS.INTERNAL_SERVER_ERROR,
    });
  }
};
