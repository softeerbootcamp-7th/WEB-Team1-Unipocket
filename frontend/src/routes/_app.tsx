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

export const Route = createFileRoute('/_app')({
  beforeLoad: async ({ context, location }) => {
    // 1. 로그인 여부 및 정지 계정 검사 (통과 못하면 '/'로 리다이렉트됨)
    const user = await requireAuth();

    const { queryClient } = context;
    const { accountBook, setAccountBook, clearAccountBook } =
      useAccountBookStore.getState();

    // 2. '/init' 페이지에 있다면 이후 가계부 조회 로직을 스킵, needsOnboarding이 true인 경우에만 접근 가능
    if (location.pathname === '/init') {
      if (!user?.needsOnboarding) {
        throw redirect({ to: '/home' });
      }
      return;
    }

    // 3. 유저 정보의 needsOnboarding을 최우선으로 체크하여 불필요한 API 호출 방지
    if (user?.needsOnboarding) {
      clearAccountBook();
      throw redirect({ to: '/init' });
    }

    // 4. 가계부 목록 조회
    const accountBooks = await queryClient.ensureQueryData(
      accountBooksQueryOptions,
    );

    // (안전장치) needsOnboarding이 false여도 가계부가 실제 0개라면 /init으로
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
