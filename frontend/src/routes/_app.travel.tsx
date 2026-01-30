import { createFileRoute } from '@tanstack/react-router';

import CellEditor from '@/components/common/data-table/CellEditor';
import { DataTable } from '@/components/common/data-table/DataTable';
import DataTableProvider from '@/components/common/data-table/DataTableProvider';
import SelectionActionProvider from '@/components/common/data-table/SelectionActionProvider';

import { columns } from '@/payments/columns';
import { type Expense, getData } from '@/payments/dummy';

export const Route = createFileRoute('/_app/travel')({
  component: RouteComponent,
});

function RouteComponent() {
  const data = getData();
  return (
    <div className="p-10">
      <DataTableProvider<Expense> columns={columns} data={data}>
        <DataTable<Expense>
          groupBy={(row) =>
            new Date(row.date).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
            })
          }
        />
        <SelectionActionProvider />
        <CellEditor />
      </DataTableProvider>
    </div>
  );
}
