import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_app/home')({
  component: RouteComponent,
});

function RouteComponent() {
  return <div>Home</div>;
}
