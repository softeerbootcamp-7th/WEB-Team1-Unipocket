import type { ReactNode } from 'react';

import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import { DataTablePagination } from '@/components/data-table/DataTablePagination';
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
  totalPages: number;
  filter: ExpenseSearchFilter;
  updateFilter: (newFilter: Partial<ExpenseSearchFilter>) => void;
  blankFallbackText?: string;
  filterActions?: ReactNode; // 우측 상단에 들어갈 추가 컴포넌트 (버튼, 메뉴 등)
  children?: ReactNode; // 하단에 들어갈 추가 컴포넌트 (액션바, 에디터 등)
}

const BaseExpenseTable = ({
  data,
  totalPages,
  filter,
  updateFilter,
  blankFallbackText,
  filterActions,
  children,
}: BaseExpenseTableProps) => {
  const currentSort = filter.sort?.[0] || 'occurredAt,desc';
  const isAmountSort = currentSort.startsWith('baseCurrencyAmount');
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
        groupBy={(row: Expense) => {
          const dateStr = new Date(row.occurredAt).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
          });

          // 금액 정렬일 경우 '날짜__baseCurrencyAmount' 형태로 키를 만들어
          // 날짜별 그룹핑을 유지하면서도 같은 날짜 내에서는 금액 순으로 정렬되도록 함
          return isAmountSort
            ? `${dateStr}__${row.baseCurrencyAmount}`
            : dateStr;
        }}
        //  화면에 보여줄 때는 '__' 뒤의 baseCurrencyAmount를 날려버리고 날짜만 표시
        groupDisplay={(groupKey: string) => groupKey.split('__')[0]}
        blankFallbackText={blankFallbackText}
      />
      <DataTablePagination
        page={filter.page ?? 0}
        totalPages={totalPages}
        onPageChange={(newPage) => updateFilter({ page: newPage })}
      />
      {children}
    </DataTableProvider>
  );
};

export default BaseExpenseTable;
