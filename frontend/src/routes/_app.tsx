import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';

import Header from '@/components/layout/Header';
import Menu from '@/components/layout/menu/Menu';
import { Skeleton } from '@/components/ui/skeleton';

import {
  accountBookDetailQueryOptions,
  accountBooksQueryOptions,
} from '@/api/account-books/query';
import { requireAuth } from '@/api/auth/api';
import { useAccountBookStore } from '@/stores/accountBookStore';
import { useRefreshStore } from '@/stores/refreshStore';

export const Route = createFileRoute('/_app')({
  beforeLoad: async ({ context, location }) => {
    const user = await requireAuth();

    const { queryClient } = context;
    const { accountBook, setAccountBook, clearAccountBook } =
      useAccountBookStore.getState();

    if (user?.needsOnboarding) {
      if (location.pathname !== '/init') {
        clearAccountBook();
        throw redirect({ to: '/init' });
      }
      return;
    }

    const accountBooks = await queryClient.fetchQuery(accountBooksQueryOptions);

    if (!accountBooks || accountBooks.length === 0) {
      clearAccountBook();
      if (location.pathname === '/init') return;
      throw redirect({ to: '/init' });
    }

    if (location.pathname === '/init') {
      throw redirect({ to: '/home' });
    }

    const storedId = accountBook?.accountBookId;
    const isStoredValid =
      !!storedId && accountBooks.some((ab) => ab.accountBookId === storedId);

    const targetId = isStoredValid ? storedId : accountBooks[0].accountBookId;
    try {
      const accountBookDetail = await queryClient.fetchQuery(
        accountBookDetailQueryOptions(targetId),
      );
      setAccountBook(accountBookDetail);
    } catch (error) {
      clearAccountBook();
      throw error;
    }
  },
  pendingComponent: () => <Skeleton className="h-dvh" />,
  component: AppLayout,
});

function AppLayout() {
  const refreshKey = useRefreshStore((s) => s.refreshKey);
  return (
    <div className="flex h-dvh overflow-hidden">
      <Menu />
      <div className="flex flex-1 flex-col">
        <Header />
        <main key={refreshKey} className="flex min-h-0 flex-1 flex-col">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
