import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import Header from '@/components/common/Header';
import Menu from '@/components/common/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

import { HTTP_STATUS } from '@/api/config/constants';
import { ApiError } from '@/api/config/error';
import { userQueryOptions } from '@/lib/auth';

export const Route = createFileRoute('/_app')({
  beforeLoad: async ({ context: { queryClient }, location }) => {
    try {
      // 1. 인증 확인 (토큰 유효성 검사 + 필요시 자동 재발급)
      // staleTime(10분)으로 매번 호출되진 않음
      await queryClient.ensureQueryData(userQueryOptions);

      // 2. 성공 시 그대로 진행 (인증된 사용자)
    } catch (error) {
      // 3. 401 에러 = 토큰 재발급까지 실패 → 로그인 페이지로
      if (
        error instanceof ApiError &&
        error.status === HTTP_STATUS.UNAUTHORIZED
      ) {
        throw redirect({
          to: '/login',
          search: { redirect: location.href },
        });
      }

      // 4. 그 외 에러는 에러 페이지로
      throw error;
    }
  },
  pendingComponent: () => <Skeleton className="h-64" />,
  component: AppLayout,
});

function AppLayout() {
  return (
    <div className="flex h-dvh overflow-hidden">
      <Menu />
      <div className="flex flex-1 flex-col">
        <Header />
        <main className="flex-1 overflow-hidden">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
