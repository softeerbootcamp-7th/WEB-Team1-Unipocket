import { createFileRoute } from '@tanstack/react-router';

import ReportPage from '@/pages/ReportPage';

export const Route = createFileRoute('/_app/report')({
  component: RouteComponent,
});

function RouteComponent() {
  return <ReportPage />;
}
