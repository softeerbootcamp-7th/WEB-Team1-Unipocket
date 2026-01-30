// context.tsx
import { createContext, useContext } from 'react';
import type { Table } from '@tanstack/react-table';

import type { TableUIAction, TableUIState } from './type';

export interface DataTableContextType<TData, TValue> {
  table: Table<TData>;
  tableState: TableUIState<TValue>;
  dispatch: React.Dispatch<TableUIAction<TValue>>;
}

export const DataTableContext = createContext<DataTableContextType<
  unknown,
  unknown
> | null>(null);

export const useDataTable = <TData, TValue>() => {
  const context = useContext(DataTableContext);

  if (!context) {
    throw new Error('useDataTable must be used within a DataTableProvider');
  }

  return context as DataTableContextType<TData, TValue>;
};
