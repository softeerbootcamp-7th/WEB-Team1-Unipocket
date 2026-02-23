import { type ComponentPropsWithoutRef, useEffect, useReducer } from 'react';
import {
  type ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  type Updater,
  useReactTable,
} from '@tanstack/react-table';

import {
  DataTableContext,
  type DataTableContextType,
} from '@/components/data-table/context';
import type { TableUIAction, TableUIState } from '@/components/data-table/type';

const applyUpdater = <T,>(updater: Updater<T>, old: T) => {
  return typeof updater === 'function'
    ? (updater as (old: T) => T)(old)
    : updater;
};

const resetCells = {
  textCell: null,
  categoryCell: null,
  amountCell: null,
  methodCell: null,
  paymentCell: null,
  travelCell: null,
};

const tableReducer = (state: TableUIState, action: TableUIAction) => {
  switch (action.type) {
    case 'SET_SELECTION_MODE':
      return { ...state, selectionMode: action.payload };
    case 'SET_ROW_SELECTION':
      return {
        ...state,
        rowSelection: applyUpdater(action.payload, state.rowSelection),
      };
    case 'SET_TEXT_CELL':
      return { ...state, ...resetCells, textCell: action.payload };
    case 'SET_CATEGORY_CELL':
      return { ...state, ...resetCells, categoryCell: action.payload };
    case 'SET_AMOUNT_CELL':
      return { ...state, ...resetCells, amountCell: action.payload };
    case 'SET_METHOD_CELL':
      return { ...state, ...resetCells, methodCell: action.payload };
    case 'SET_TRAVEL_CELL':
      return { ...state, ...resetCells, travelCell: action.payload };
    case 'SET_ACTIVE_ROW':
      return { ...state, activeRow: action.payload };
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
  const [tableState, dispatch] = useReducer(tableReducer, {
    selectionMode: null,
    rowSelection: {},
    activeRow: null,
    ...resetCells, // 초기 상태: 모든 에디터 닫힘
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
    meta: {
      dispatch,
    },
  });

  return (
    <DataTableContext.Provider
      value={
        {
          table,
          tableState,
          dispatch,
        } as DataTableContextType<unknown>
      }
    >
      <div className="flex min-h-0 flex-1 flex-col overflow-hidden">
        {children}
      </div>
    </DataTableContext.Provider>
  );
};

export default DataTableProvider;
