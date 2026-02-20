import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import AmountCellEditor from '@/components/data-table/editors/AmountCellEditor';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import PaymentCellEditor from '@/components/data-table/editors/PaymentCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';
import SelectionActionBar from '@/components/data-table/SelectionActionBar';
import { columns } from '@/components/home-page/columns';
import TableSidePanel from '@/components/side-panel/TableSidePanel';
import UploadMenu from '@/components/upload/UploadMenu';

import { useGetExpensesQuery } from '@/api/expenses/query';
import type { Expense } from '@/api/expenses/type';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const ExpenseTable = () => {
  const { accountBook } = useAccountBookStore();

  const { data } = useGetExpensesQuery(accountBook!.id, {
    page: 0,
    size: 50,
  });

  return (
    <div className="bg-background-normal relative flex min-h-0 flex-1 flex-col px-2 pt-4">
      <DataTableProvider columns={columns} data={data.expenses}>
        <DataTableFilterProvider>
          <DateFilter />
          <MerchantFilter />
          <CategoryFilter />
          <MethodFilter />
          <div className="flex-1" />
          <SortDropdown />
          <UploadMenu />
        </DataTableFilterProvider>
        <DataTable
          groupBy={(row: Expense) =>
            new Date(row.occurredAt).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
            })
          }
        />
        <SelectionActionBar />
        <TextCellEditor />
        <AmountCellEditor />
        <CategoryCellEditor />
        <PaymentCellEditor />
        <TableSidePanel />
      </DataTableProvider>
    </div>
  );
};

export default ExpenseTable;
