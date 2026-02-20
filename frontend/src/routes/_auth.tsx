import { createFileRoute, Outlet } from '@tanstack/react-router';

import LandingHeader from '@/components/landing-page/LandingHeader';
import { Skeleton } from '@/components/ui/skeleton';

import { redirectIfAuthenticated } from '@/api/auth/api';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async () => {
    await redirectIfAuthenticated();
  },
  pendingComponent: () => <Skeleton className="h-dvh" />,
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
