import { useEffect } from 'react';
import { useRouter } from '@tanstack/react-router';

import { HTTP_STATUS } from '@/api/config/constants';
import { ApiError } from '@/api/config/error';

interface ErrorFallbackProps {
  error: Error;
  reset: () => void;
  title?: string;
}

export function ErrorFallback({
  error,
  reset,
  title = '오류가 발생했습니다',
}: ErrorFallbackProps) {
  const router = useRouter();

  // 401 에러 발생 시 로그인 페이지로 이동 (useEffect 사용)
  useEffect(() => {
    if (
      error instanceof ApiError &&
      error.status === HTTP_STATUS.UNAUTHORIZED
    ) {
      const currentPath = window.location.pathname;

      // public 경로가 아니면 로그인으로 강제 이동
      // '/' 체크는 정확히 일치해야 하고, '/login'은 startsWith로 체크
      const isPublicPath =
        currentPath === '/' || currentPath.startsWith('/login');

      if (!isPublicPath) {
        window.location.href = '/login';
      }
    }
  }, [error]);

  // 401 에러일 때는 UI를 렌더링하지 않음 (리다이렉트 중)
  if (error instanceof ApiError && error.status === HTTP_STATUS.UNAUTHORIZED) {
    return null;
  }

  const handleRetry = () => {
    reset(); // React Query 등의 에러 상태 초기화
    router.invalidate(); // 라우터 데이터 다시 불러오기
  };

  return (
    <div className="m-4 rounded border border-red-200 bg-red-50 p-4">
      <h2 className="font-bold text-red-800">{title}</h2>
      <p className="mb-4 text-red-600">{error.message}</p>
      <button
        onClick={handleRetry}
        className="rounded bg-red-600 px-4 py-2 text-white transition-colors hover:bg-red-700"
      >
        다시 시도
      </button>
    </div>
  );
}
