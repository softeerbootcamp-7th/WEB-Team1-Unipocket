import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_app/init')({
  component: RouteComponent,
})

function RouteComponent() {
  return <div>Hello "/init"!</div>
}
