import type { ReactNode } from 'react';
import type { ColumnDef } from '@tanstack/react-table';

import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import { DataTablePagination } from '@/components/data-table/DataTablePagination';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';

import type { ExpenseSearchFilter } from '@/api/expenses/type';

interface BaseExpenseTableProps<TData> {
  data: TData[];
  columns: ColumnDef<TData, unknown>[];
  totalPages: number;
  filter: ExpenseSearchFilter;
  updateFilter: (newFilter: Partial<ExpenseSearchFilter>) => void;
  blankFallbackText?: string;
  filterActions?: ReactNode;
  children?: ReactNode;
  hideFilters?: boolean;
  groupBy?: (row: TData) => string;
  groupDisplay?: (groupKey: string) => string;
}

const BaseExpenseTable = <TData,>({
  data,
  columns,
  totalPages,
  filter,
  updateFilter,
  hideFilters = false,
  blankFallbackText,
  filterActions,
  children,
  groupBy,
  groupDisplay,
}: BaseExpenseTableProps<TData>) => {
  return (
    <DataTableProvider columns={columns} data={data}>
      <DataTableFilterProvider filter={filter} updateFilter={updateFilter}>
        {!hideFilters && (
          <>
            <DateFilter />
            <MerchantFilter />
            <CategoryFilter />
            <MethodFilter />
            <div className="flex-1" />
            <SortDropdown />
          </>
        )}
        {filterActions}
      </DataTableFilterProvider>

      <DataTable
        groupBy={groupBy}
        groupDisplay={groupDisplay}
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
