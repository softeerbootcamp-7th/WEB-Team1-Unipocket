import { createFileRoute, Outlet } from '@tanstack/react-router';

import Menu from '@/components/common/menu/Menu';

export const Route = createFileRoute('/_app')({
  component: AppLayout,
});

function AppLayout() {
  return (
    <div className="flex">
      <Menu />
      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  );
}
