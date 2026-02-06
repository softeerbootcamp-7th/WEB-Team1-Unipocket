import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import LandingHeader from '@/components/landing-page/LandingHeader';
import { Skeleton } from '@/components/ui/skeleton';

import { HTTP_STATUS } from '@/api/config/constants';
import { ApiError } from '@/api/config/error';
import { userQueryOptions } from '@/lib/auth';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async ({ context: { queryClient } }) => {
    try {
      // staleTime을 무시하고 최신 상태 확인 (revalidate: true)
      await queryClient.fetchQuery(userQueryOptions);

      // 성공 시 = 실제로 로그인되어 있음 → 홈으로 리다이렉트
      throw redirect({
        to: '/home',
      });
    } catch (error) {
      // redirect 에러는 다시 던져야 라우터가 처리함
      if (error instanceof Error && 'isRedirect' in error) {
        throw error;
      }

      // 401 에러 = 로그인 안 됨 → 정상 흐름 (로그인 페이지 보여주기)
      if (
        error instanceof ApiError &&
        error.status === HTTP_STATUS.UNAUTHORIZED
      ) {
        // 401이면 캐시 삭제 (다음에 혼란 방지)
        queryClient.removeQueries({ queryKey: userQueryOptions.queryKey });
        return; // 로그인 페이지로 진입 허용
      }

      // 그 외 에러는 에러 페이지로
      throw error;
    }
  },
  pendingComponent: () => <Skeleton className="h-64" />,
  component: AppLayout,
});

function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <LandingHeader />
      <main className="flex-1 overflow-hidden">
        <Outlet />
      </main>
    </div>
  );
}
