import { createFileRoute } from '@tanstack/react-router';

import LoginPage from '@/pages/LoginPage';

interface LoginSearch {
  error?: string;
  provider?: string;
}

export const Route = createFileRoute('/_auth/login')({
  validateSearch: (search: Record<string, unknown>): LoginSearch => {
    return {
      error: search.error as string | undefined,
      provider: search.provider as string | undefined,
    };
  },
  component: RouteComponent,
});

function RouteComponent() {
  return <LoginPage />;
}
