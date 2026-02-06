import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import Header from '@/components/common/Header';
import Menu from '@/components/common/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

import { isAccessTokenValid } from '@/lib/auth';

export const Route = createFileRoute('/_app')({
  beforeLoad: async ({ location }) => {
    // 쿠키에서 토큰 만료시간을 확인하여 유효성 검사
    // 브라우저가 만료된 쿠키를 자동으로 삭제하므로
    // 쿠키가 존재하면 유효한 것으로 간주
    if (!isAccessTokenValid()) {
      throw redirect({
        to: '/login',
        search: { redirect: location.href },
      });
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
