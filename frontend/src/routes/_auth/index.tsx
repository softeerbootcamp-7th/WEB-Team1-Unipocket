import { createFileRoute } from '@tanstack/react-router';

import LandingPage from '@/pages/LandingPage';

export const Route = createFileRoute('/_auth/')({
  component: RouteComponent,
});

function RouteComponent() {
  return <LandingPage />;
}
