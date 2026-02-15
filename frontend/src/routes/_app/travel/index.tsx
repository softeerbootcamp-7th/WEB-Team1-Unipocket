import { createFileRoute } from '@tanstack/react-router';

import TravelPage from '@/pages/TravelPage';

export const Route = createFileRoute('/_app/travel/')({
  component: RouteComponent,
});

function RouteComponent() {
  return <TravelPage />;
}
