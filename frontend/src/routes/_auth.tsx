import {
  createFileRoute,
  isRedirect,
  Outlet,
  redirect,
} from '@tanstack/react-router';

import LandingHeader from '@/components/landing-page/LandingHeader';
import { Skeleton } from '@/components/ui/skeleton';

import { getUser } from '@/api/user/api';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async () => {
    const redirectIfAuthenticated = async () => {
      try {
        // useQuery 사용하지 않는 이유는 캐싱된 데이터를 사용하지 않고
        // 항상 최신 인증 상태를 확인하기 위함
        const user = await getUser();
        if (user) {
          throw redirect({
            to: '/home',
          });
        }
      } catch (error) {
        // redirect 에러는 다시 throw하여 라우터가 처리하도록 함
        if (isRedirect(error)) {
          throw error;
        }
        // 인증되지 않은 상태(401 등)이면 아무 작업도 하지 않음
      }
    };
    await redirectIfAuthenticated();
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
