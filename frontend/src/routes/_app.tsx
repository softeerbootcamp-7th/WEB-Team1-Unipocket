import { createFileRoute, Outlet } from '@tanstack/react-router';

import Header from '@/components/common/Header';
import Menu from '@/components/common/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

import { requireAuth } from '@/lib/auth';

export const Route = createFileRoute('/_app')({
  // 🔒 로그인 체크 로직
  beforeLoad: async ({ location }) => {
    // API 응답이 성공했지만 유저 정보가 없는 경우에만 수동 리다이렉트 (lib/auth.ts로 위임)
    const user = await requireAuth(location.href);

    return { user };
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
