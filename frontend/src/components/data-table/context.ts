import type { Table } from '@tanstack/react-table';
import { createContext, useContext } from 'react';
import type { TableUIAction, TableUIState } from './type';

interface DataTableContextType<TData> {
  table: Table<TData>;
  tableState: TableUIState;
  dispatch: React.Dispatch<TableUIAction>;
}

export const DataTableContext = createContext<DataTableContextType<any> | null>(
  null,
);

export const useDataTable = () => {
  const context = useContext(DataTableContext);
  if (!context) {
    throw new Error('useDataTable must be used within a DataTableProvider');
  }
  return context;
};
