import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_app/travel')({
  component: RouteComponent,
});

function RouteComponent() {
  return <div>Hello "/travel"!</div>;
}
