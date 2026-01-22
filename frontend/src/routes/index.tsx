import { DataTable } from '@/components/data-table/DataTable';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import FloatingActionProvider from '@/components/data-table/FloatingActionProvider';
import { columns } from '@/payments/columns';
import { getData } from '@/payments/page';
import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/')({
  component: RouteComponent,
});

function RouteComponent() {
  const data = getData();
  return (
    <div className="container mx-auto py-10">
      <DataTableProvider
        columns={columns}
        data={data}
        floatingBarVariant="MANAGEMENT"
      >
        <DataTable />
        <FloatingActionProvider />
      </DataTableProvider>
    </div>
  );
}
