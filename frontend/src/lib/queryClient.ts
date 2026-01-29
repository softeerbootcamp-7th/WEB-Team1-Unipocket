// src/queryClient.ts
import {
  type DefaultError,
  MutationCache,
  QueryCache,
  QueryClient,
} from '@tanstack/react-query';

import { ApiError } from '@/apis/error';

// 공통 에러 처리 함수
const handleGlobalError = (error: DefaultError) => {
  if (error instanceof ApiError) {
    if (error.status === 401) {
      console.log('인증 만료! 로그인 페이지로 이동합니다.');
      queryClient.clear();
      window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
    }
  }
};

export const queryClient = new QueryClient({
  // 조회(Query) 중 에러 발생 시
  queryCache: new QueryCache({
    onError: handleGlobalError,
  }),
  // 수정/삭제(Mutation) 중 에러 발생 시
  mutationCache: new MutationCache({
    onError: handleGlobalError,
  }),
  defaultOptions: {
    queries: {
      retry: false, // 401인데 재시도하면 서버만 아프니까 끔
    },
  },
});
