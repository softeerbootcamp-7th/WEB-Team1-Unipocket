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
  params?: Record<string, string | number>;
  options?: RequestInit;
  isRetry?: boolean; // 재시도 여부 플래그 -> 401 재발급 시도 무한 루프 방지용
}

// 1. 재발급 요청의 Promise를 저장할 변수 (싱글톤 패턴처럼 동작)
let refreshPromise: Promise<void> | null = null;

// 2. 401 발생 시 토큰 재발급 요청을 처리하는 함수
const refreshAccessToken = async (): Promise<void> => {
  // 이미 재발급이 진행 중이라면, 해당 Promise를 반환하여 기다리게 함
  if (refreshPromise) {
    return refreshPromise;
  }

  // 재발급이 진행 중이 아니라면 새로운 요청 시작 및 Promise 저장
  refreshPromise = (async () => {
    try {
      await customFetch({
        endpoint: '/auth/reissue',
        options: { method: 'POST' },
        isRetry: true, // customFetch를 사용하되 isRetry=true로 설정하여 계속 401이 응답으로 오면 무한 루프 방지
      });
    } finally {
      // 성공하든 실패하든 요청이 끝나면 변수를 초기화하여
      // 다음 401 발생 시 다시 재발급을 시도할 수 있게 함
      refreshPromise = null;
    }
  })();

  return refreshPromise;
};

export const customFetch = async <T>({
  endpoint,
  params,
  options = {},
  isRetry = false,
}: customFetchParams): Promise<T> => {
  const { headers, ...restOptions } = options;

  // 타임아웃 설정을 위한 AbortController
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), DEFAULT_TIMEOUT);

  // 1. 쿼리 스트링 처리 로직 분리 및 정렬 적용
  let queryString = '';
  if (params) {
    const searchParams = new URLSearchParams(params as Record<string, string>);
    searchParams.sort(); // 알파벳 순으로 파라미터 정렬 (캐싱 효율 상승)
    queryString = `?${searchParams.toString()}`;
  }

  const cleanEndpoint = endpoint.replace(/^\//, '');
  const url = `${API_BASE_URL}/${cleanEndpoint}${queryString}`;
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
        await refreshAccessToken();

        // 재발급 성공 시 원래 요청 재시도 (isRetry=true로 설정)
        return await customFetch<T>({ endpoint, options, isRetry: true });
      } catch {
        // 재발급 실패 시 401 에러 발생
        throw new ApiError({
          status: HTTP_STATUS.UNAUTHORIZED,
        });
      }
    }

    // 401 재발급 재시도 중에도 401이 발생하면 바로 에러
    if (response.status === HTTP_STATUS.UNAUTHORIZED && isRetry) {
      throw new ApiError({
        status: HTTP_STATUS.UNAUTHORIZED,
      });
    }

    // HTTP 에러 처리 (400~500번대)
    if (!response.ok) {
      // JSON 파싱이 실패하더라도 에러를 내지 않고 null을 반환하게 만든 안전장치
      const status = response.status;
      const errorBody = await response.json().catch(() => null);
      const message = errorBody?.message || getDefaultErrorMessage(status);

      throw new ApiError({ message, status });
    }

    // 3. 정상 응답 처리 (200~299)
    if (response.status === HTTP_STATUS.NO_CONTENT) return undefined as T;
    return await response.json();
  } catch (error) {
    clearTimeout(timeoutId); // 에러 발생 시에도 타이머 해제 필수

    // 타임아웃 에러 처리 (AbortError)
    if (error instanceof Error && error.name === ERROR_NAMES.ABORT_ERROR) {
      throw new ApiError({
        status: HTTP_STATUS.REQUEST_TIMEOUT,
      });
    }

    // 이미 ApiError라면 그대로 던지고
    if (error instanceof ApiError) {
      throw error;
    }

    // 그 외는 네트워크 에러로 간주
    throw new ApiError({
      status: HTTP_STATUS.INTERNAL_SERVER_ERROR,
    });
  }
};
