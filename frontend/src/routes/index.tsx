import { createFileRoute, redirect } from '@tanstack/react-router';

import { ErrorFallback } from '@/components/common/ErrorFallback';
import { Skeleton } from '@/components/ui/skeleton';

export const Route = createFileRoute('/')({
  beforeLoad: async ({ location }) => {
    // 실제 API를 호출하는 코드
    // const { queryClient } = context;
    // const user = await queryClient.ensureQueryData(userQueryOptions);  // 여기서 발생하는 401 에러는 전역 에러 핸들러가 처리하거나 errorComponent가 잡습니다.

    // API 대신 사용할 임시 유저 데이터
    const mockUser = {
      id: '1',
      name: '홍길동',
      email: 'honggildong@example.com',
    };

    // 실제 API 호출 느낌을 내기 위한 0.5초 대기 로직
    const user = await new Promise<typeof mockUser | null>((resolve) => {
      setTimeout(() => {
        resolve(mockUser); // 0.5초 뒤에 홍길동 데이터를 반환
        // resolve(null); // 만약 로그인 안 된 상태를 테스트하고 싶다면 null을 넣으세요.
      }, 500);
    });

    if (user) {
      throw redirect({
        to: '/home',
        search: { redirect: location.href },
      });
    }

    return { user };
  },
  pendingComponent: () => <Skeleton className="h-64" />,
  errorComponent: ({ error, reset }) => {
    return (
      <ErrorFallback
        error={error}
        reset={reset}
        title="서비스 이용에 불편을 드려 죄송합니다."
      />
    );
  },
  component: RouteComponent,
});

function RouteComponent() {
  return <div>Unipocket</div>;
}
