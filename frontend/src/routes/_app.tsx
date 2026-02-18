import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import Header from '@/components/layout/Header';
import Menu from '@/components/layout/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

import { accountBooksQueryOptions } from '@/api/account-books/query';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

export const Route = createFileRoute('/_app')({
  beforeLoad: async ({ context }) => {
    const { queryClient } = context;
    const data = await queryClient.ensureQueryData(accountBooksQueryOptions);
    const { accountBook, setAccountBook } = useAccountBookStore.getState();

    if (!data || data.length === 0) {
      throw redirect({
        to: '/init',
      });
    }

    if (!accountBook) {
      const defaultBook = data.find((book) => book.isMain) || data[0];
      setAccountBook(defaultBook);
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
