import type { QueryClient } from '@tanstack/react-query';
import { createRootRouteWithContext, Outlet } from '@tanstack/react-router';

import { ErrorFallback } from '@/components/common/ErrorFallback';

export interface MyRouterContext {
  queryClient: QueryClient;
}
export const Route = createRootRouteWithContext<MyRouterContext>()({
  errorComponent: ({ error, reset }) => {
    return (
      <ErrorFallback
        error={error}
        reset={reset}
        title="서비스 이용에 불편을 드려 죄송합니다."
      />
    );
  },
  component: Rootayout,
});

function Rootayout() {
  return (
    <main className="bg-background-alternative h-full">
      <Outlet />
    </main>
  );
}
