import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import Header from '@/components/layout/Header';
import Menu from '@/components/layout/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

import {
  accountBookDetailQueryOptions,
  accountBooksQueryOptions,
} from '@/api/account-books/query';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

export const Route = createFileRoute('/_app')({
  beforeLoad: async ({ context }) => {
    const { queryClient } = context;
    const accountBooks = await queryClient.ensureQueryData(
      accountBooksQueryOptions,
    );
    const { accountBook, setAccountBook } = useAccountBookStore.getState();

    if (!accountBooks || accountBooks.length === 0) {
      throw redirect({
        to: '/init',
      });
    }

    if (!accountBook) {
      const accountBookDetail = await queryClient.ensureQueryData(
        accountBookDetailQueryOptions(accountBooks[0].id),
      );

      setAccountBook(accountBookDetail);
    }
  },
  pendingComponent: () => <Skeleton className="h-dvh" />,
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
