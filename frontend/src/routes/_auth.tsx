import { createFileRoute, Outlet } from '@tanstack/react-router';

import LandingHeader from '@/components/landing-page/LandingHeader';
import { Skeleton } from '@/components/ui/skeleton';

import { redirectIfAuthenticated } from '@/lib/auth';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async () => {
    const user = await redirectIfAuthenticated();
    return { user };
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
