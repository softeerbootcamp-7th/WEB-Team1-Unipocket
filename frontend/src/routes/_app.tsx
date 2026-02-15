import { createFileRoute, Outlet } from '@tanstack/react-router';

import Header from '@/components/layout/Header';
import Menu from '@/components/layout/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

export const Route = createFileRoute('/_app')({
  pendingComponent: () => <Skeleton className="h-64" />,
  component: AppLayout,
});

function AppLayout() {
  return (
    <div className="flex h-dvh overflow-hidden">
      <Menu />
      <div className="flex flex-1 flex-col">
        <Header />
        <main className="flex min-h-0 flex-1 flex-col">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
