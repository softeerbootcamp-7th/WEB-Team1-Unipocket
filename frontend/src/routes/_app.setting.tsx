import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_app/setting')({
  component: RouteComponent,
})

function RouteComponent() {
  return <div>Hello "/setting"!</div>
}
