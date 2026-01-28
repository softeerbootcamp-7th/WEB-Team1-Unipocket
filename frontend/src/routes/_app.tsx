import { createFileRoute, Outlet } from '@tanstack/react-router';

import Menu from '@/components/common/menu/Menu';
import Header from '@/components/common/Header';

export const Route = createFileRoute('/_app')({
  component: AppLayout,
});

function AppLayout() {
  return (
    <div className="flex">
      <Menu />
      <div className="flex flex-1 flex-col">
        <Header />
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
