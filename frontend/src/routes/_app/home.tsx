import { createFileRoute } from '@tanstack/react-router';

import ReportCategory from '@/components/report-page/category/ReportCategory';

export const Route = createFileRoute('/_app/home')({
  component: RouteComponent,
});

function RouteComponent() {
  return <ReportCategory />;
}
