import { createFileRoute } from '@tanstack/react-router';

import { DataTable } from '@/components/ui/data-table/DataTable';
import DataTableCellEditor from '@/components/ui/data-table/DataTableCellEditor';
import DataTableProvider from '@/components/ui/data-table/DataTableProvider';
import { columns } from '@/components/ui/data-table/payments/columns';
import {
  type Expense,
  getData,
} from '@/components/ui/data-table/payments/dummy';
import SelectionActionProvider from '@/components/ui/data-table/SelectionActionProvider';

export const Route = createFileRoute('/_app/travel')({
  component: RouteComponent,
});

function RouteComponent() {
  const data = getData();
  return (
    <div className="h-screen overflow-y-auto p-10">
      <DataTableProvider columns={columns} data={data}>
        <DataTable
          groupBy={(row: Expense) =>
            new Date(row.date).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
            })
          }
        />
        <SelectionActionProvider />
        <DataTableCellEditor />
      </DataTableProvider>
    </div>
  );
}
