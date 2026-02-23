import type { ReactNode } from 'react';

import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';
import { columns } from '@/components/home-page/columns';

import type { Expense, ExpenseSearchFilter } from '@/api/expenses/type';

interface BaseExpenseTableProps {
  data: Expense[];
  filter: ExpenseSearchFilter;
  updateFilter: (newFilter: Partial<ExpenseSearchFilter>) => void;
  blankFallbackText?: string;
  filterActions?: ReactNode; // 우측 상단에 들어갈 추가 컴포넌트 (버튼, 메뉴 등)
  children?: ReactNode; // 하단에 들어갈 추가 컴포넌트 (액션바, 에디터 등)
}

const BaseExpenseTable = ({
  data,
  filter,
  updateFilter,
  blankFallbackText,
  filterActions,
  children,
}: BaseExpenseTableProps) => {
  return (
    <DataTableProvider columns={columns} data={data}>
      <DataTableFilterProvider filter={filter} updateFilter={updateFilter}>
        <DateFilter />
        <MerchantFilter />
        <CategoryFilter />
        <MethodFilter />
        <div className="flex-1" />
        <SortDropdown />
        {filterActions}
      </DataTableFilterProvider>

      <DataTable
        groupBy={(row: Expense) =>
          new Date(row.occurredAt).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
          })
        }
        blankFallbackText={blankFallbackText}
      />
      {children}
    </DataTableProvider>
  );
};

export default BaseExpenseTable;
