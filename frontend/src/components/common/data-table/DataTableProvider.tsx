import { type ComponentPropsWithoutRef, useEffect, useReducer } from 'react';
import {
  type ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  type Updater,
  useReactTable,
} from '@tanstack/react-table';

import { DataTableContext, type DataTableContextType } from './context';
import type { TableUIAction, TableUIState } from './type';

const applyUpdater = <T,>(updater: Updater<T>, old: T) => {
  return typeof updater === 'function'
    ? (updater as (old: T) => T)(old)
    : updater;
};

const tableReducer = <TValue,>(
  state: TableUIState<TValue>,
  action: TableUIAction<TValue>,
) => {
  switch (action.type) {
    case 'SET_SELECTION_MODE':
      return { ...state, selectionMode: action.payload };
    case 'SET_ROW_SELECTION':
      return {
        ...state,
        rowSelection: applyUpdater(action.payload, state.rowSelection),
      };
    case 'SET_ACTIVE_CELL':
      return {
        ...state,
        activeCell: action.payload,
      };
    default:
      return state;
  }
};

interface DataTableProviderProps<
  TData,
  TValue,
> extends ComponentPropsWithoutRef<'div'> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
}

const DataTableProvider = <TData, TValue = unknown>({
  columns,
  data,
  children,
}: DataTableProviderProps<TData, TValue>) => {
  const [tableState, dispatch] = useReducer(tableReducer<TValue>, {
    selectionMode: null,
    rowSelection: {},
    activeCell: null,
  });

  // 행 선택 상태에 따른 자동 바 노출 로직 (useEffect)
  useEffect(() => {
    const selectedCount = Object.keys(tableState.rowSelection).length;

    if (selectedCount > 0) {
      dispatch({ type: 'SET_SELECTION_MODE', payload: 'MANAGEMENT' });
    } else {
      dispatch({ type: 'SET_SELECTION_MODE', payload: null });
    }
  }, [tableState.rowSelection]);

  const table = useReactTable<TData>({
    data,
    columns,
    state: {
      rowSelection: tableState.rowSelection,
    },
    onRowSelectionChange: (updater) =>
      dispatch({ type: 'SET_ROW_SELECTION', payload: updater }),
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    manualFiltering: false,
  });

  return (
    <DataTableContext.Provider
      value={
        {
          table,
          tableState,
          dispatch,
        } as DataTableContextType<unknown, unknown>
      }
    >
      {children}
    </DataTableContext.Provider>
  );
};

export default DataTableProvider;
