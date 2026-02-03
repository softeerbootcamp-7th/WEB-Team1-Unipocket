import { createFileRoute } from '@tanstack/react-router';

import Money from '@/components/common/side-panel/Money';

export const Route = createFileRoute('/_app/home')({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <div className="p-4">
      <Money />
    </div>
  );
}
