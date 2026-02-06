import { createFileRoute } from '@tanstack/react-router';

import Homepage from '@/pages/Homepage';

export const Route = createFileRoute('/_app/home')({
  component: RouteComponent,
});

function RouteComponent() {
  return <Homepage />;
}
