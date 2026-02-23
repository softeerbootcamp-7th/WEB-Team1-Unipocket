import { createContext, useContext } from 'react';
import type { Table } from '@tanstack/react-table';

import type { TableUIAction, TableUIState } from '@/components/data-table/type';

import type { ExpenseSearchFilter } from '@/api/expenses/type';

export interface DataTableContextType<TData> {
  table: Table<TData>;
  tableState: TableUIState;
  dispatch: React.Dispatch<TableUIAction>;
}

export const DataTableContext =
  createContext<DataTableContextType<unknown> | null>(null);

export const useDataTable = <TData>() => {
  const context = useContext(DataTableContext);

  if (!context) {
    throw new Error('useDataTable must be used within a DataTableProvider');
  }

  return context as DataTableContextType<TData>;
};

interface DataTableFilterContextType {
  filter: ExpenseSearchFilter;
  updateFilter: (newFilter: Partial<ExpenseSearchFilter>) => void;
}

export const DataTableFilterContext =
  createContext<DataTableFilterContextType | null>(null);

export const useDataTableFilter = () => {
  const context = useContext(DataTableFilterContext);
  if (!context) {
    throw new Error(
      'useDataTableFilter must be used within a DataTableFilterProvider',
    );
  }
  return context;
};
