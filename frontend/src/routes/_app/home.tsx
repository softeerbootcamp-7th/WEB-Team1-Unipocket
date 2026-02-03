import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_app/home')({
  component: RouteComponent,
});

function RouteComponent() {
  return <div className="p-4">Home</div>;
}
