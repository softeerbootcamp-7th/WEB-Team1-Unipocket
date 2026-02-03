import { createFileRoute } from '@tanstack/react-router';

import LandingHeader from '@/components/landing-page/LandingHeader';
import { Skeleton } from '@/components/ui/skeleton';

import LandingPage from '@/pages/LandingPage';

import { redirectIfAuthenticated } from '@/lib/auth';

export const Route = createFileRoute('/')({
  beforeLoad: async () => {
    const user = await redirectIfAuthenticated();
    return { user };
  },
  pendingComponent: () => <Skeleton className="h-64" />,
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-10">
        <LandingHeader />
      </header>
      <main>
        <LandingPage />
      </main>
    </div>
  );
}
