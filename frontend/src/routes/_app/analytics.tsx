import { createFileRoute } from '@tanstack/react-router';

import AnalyticsPage from '@/pages/AnalyticsPage';

export const Route = createFileRoute('/_app/analytics')({
  component: RouteComponent,
});

function RouteComponent() {
  return <AnalyticsPage />;
}