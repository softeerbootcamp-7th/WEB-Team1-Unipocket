import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import Header from '@/components/layout/Header';
import Menu from '@/components/layout/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

import {
  accountBookDetailQueryOptions,
  accountBooksQueryOptions,
} from '@/api/account-books/query';
import { useAccountBookStore } from '@/stores/accountBookStore';

export const Route = createFileRoute('/_app')({
  beforeLoad: async ({ context, location }) => {
    if (location.pathname === '/init') {
      return;
    }

    const { queryClient } = context;
    const accountBooks = await queryClient.ensureQueryData(
      accountBooksQueryOptions,
    );
    const { accountBook, setAccountBook, clearAccountBook } =
      useAccountBookStore.getState();

    if (!accountBooks || accountBooks.length === 0) {
      clearAccountBook();
      throw redirect({ to: '/init' });
    }

    let targetId = accountBook?.id;

    if (!targetId || !accountBooks.some((ab) => ab.id === targetId)) {
      targetId = accountBooks[0].id;
    }

    try {
      const accountBookDetail = await queryClient.ensureQueryData(
        accountBookDetailQueryOptions(targetId),
      );
      setAccountBook(accountBookDetail);
    } catch (error) {
      clearAccountBook();
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
        <main className="flex min-h-0 flex-1 flex-col">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
