import { createFileRoute } from '@tanstack/react-router';

import { DataTable } from '@/components/common/data-table/DataTable';
import DataTableCellEditor from '@/components/common/data-table/DataTableCellEditor';
import DataTableProvider from '@/components/common/data-table/DataTableProvider';
import { columns } from '@/components/common/data-table/payments/columns';
import {
  type Expense,
  getData,
} from '@/components/common/data-table/payments/dummy';
import SelectionActionProvider from '@/components/common/data-table/SelectionActionProvider';

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
