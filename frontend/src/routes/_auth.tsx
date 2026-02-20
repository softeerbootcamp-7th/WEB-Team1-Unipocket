import { createFileRoute, Outlet } from '@tanstack/react-router';

import LandingHeader from '@/components/landing-page/LandingHeader';
import { Skeleton } from '@/components/ui/skeleton';

import { requireGuest } from '@/api/auth/api';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async () => {
    // 이미 로그인된 유저라면 여기서 /home 또는 /init으로 튕겨나갑니다.
    await requireGuest();
  },
  pendingComponent: () => <Skeleton className="h-64" />,
  component: AppLayout,
});

function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <LandingHeader />
      <main className="flex flex-1 flex-col overflow-hidden">
        <Outlet />
      </main>
    </div>
  );
}
