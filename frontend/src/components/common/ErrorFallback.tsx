// src/components/common/ErrorFallback.tsx
import { useRouter } from '@tanstack/react-router';

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
