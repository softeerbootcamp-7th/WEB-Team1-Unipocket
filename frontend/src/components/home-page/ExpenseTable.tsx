import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';
import { columns } from '@/components/home-page/columns';
import UploadMenu from '@/components/upload/UploadMenu';

import { useGetExpensesSuspenseQuery } from '@/api/expenses/query';
import type { ExpenseResponse } from '@/api/expenses/type';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const ExpenseTable = () => {
  const { accountBook } = useAccountBookStore();

  const { data } = useGetExpensesSuspenseQuery(accountBook!.id, {
    page: 0,
    size: 50,
  });

  return (
    <div className="bg-background-normal relative rounded-2xl px-2 py-4 shadow">
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
          groupBy={(row: ExpenseResponse) =>
            new Date(row.occurredAt).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
            })
          }
        />
      </DataTableProvider>
    </div>
  );
};

export default ExpenseTable;
