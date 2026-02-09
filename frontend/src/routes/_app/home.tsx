import { createFileRoute } from '@tanstack/react-router';

import ComparisonChart from '@/components/common/chart/comparison/ComparisonChart';

export const Route = createFileRoute('/_app/home')({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <div className="flex gap-3 p-5">
      <ComparisonChart />
      <ComparisonChart />
    </div>
  );
}