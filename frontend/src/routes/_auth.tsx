import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import LandingHeader from '@/components/landing-page/LandingHeader';
import { Skeleton } from '@/components/ui/skeleton';

import { isAccessTokenValid } from '@/lib/auth';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async () => {
    // 쿠키에서 토큰 만료시간을 확인하여 유효성 검사
    // 이미 로그인되어 있으면 홈으로 리다이렉트
    if (isAccessTokenValid()) {
      throw redirect({
        to: '/home',
      });
    }
    // 토큰이 없거나 만료되었으면 로그인 페이지 진입 허용
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
