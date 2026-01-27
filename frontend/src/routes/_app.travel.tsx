import { createFileRoute } from '@tanstack/react-router';

import { DataTable } from '@/components/common/data-table/DataTable';
import DataTableProvider from '@/components/common/data-table/DataTableProvider';
import FloatingActionProvider from '@/components/common/data-table/FloatingActionProvider';

import { columns } from '@/payments/columns';
import { getData } from '@/payments/dummy';

export const Route = createFileRoute('/_app/travel')({
  component: RouteComponent,
});

function RouteComponent() {
  const data = getData();
  return (
    <div className="p-10">
      <DataTableProvider
        columns={columns}
        data={data}
        floatingBarVariant={'MANAGEMENT'}
      >
        <DataTable />
        <FloatingActionProvider />
      </DataTableProvider>
    </div>
  );
}
