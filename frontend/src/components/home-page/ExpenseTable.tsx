import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
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
import { useRequiredAccountBook } from '@/stores/useAccountBookStore';

const ExpenseTable = () => {
  const accountBookId = useRequiredAccountBook().id;

  const { data } = useGetExpensesQuery(accountBookId, {
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
        <TableSidePanel />
      </DataTableProvider>
    </div>
  );
};

export default ExpenseTable;
