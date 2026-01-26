import { useEffect, useReducer } from 'react';
import {
  type ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  useReactTable,
} from '@tanstack/react-table';

import { DataTableContext } from './context';
import type { FloatingBarVariant, TableUIAction, TableUIState } from './type';

interface DataTableProviderProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  floatingBarVariant: FloatingBarVariant;
  children: React.ReactNode;
}

const tableReducer = (state: TableUIState, action: TableUIAction) => {
  switch (action.type) {
    case 'SET_BAR_VARIANT':
      return { ...state, floatingBarVariant: action.payload };
    case 'SET_FILTERS':
      return { ...state, filters: [...(state.filters || []), action.payload] };
    case 'RESET_FILTERS':
      return {
        ...state,
        filters: [
          ...(state.filters || []).filter((filter) => filter != action.payload),
        ],
      };
    case 'TOGGLE_ROW_SELECTION':
      // 특정 행 선택 토글 로직
      return state;
    case 'SET_SORTING':
      return {
        ...state,
        sorting:
          typeof action.payload === 'function'
            ? action.payload(state.sorting)
            : action.payload,
      };
    case 'SET_ROW_SELECTION':
      return {
        ...state,
        rowSelection:
          typeof action.payload === 'function'
            ? action.payload(state.rowSelection)
            : action.payload,
      };
    default:
      return state;
  }
};

const DataTableProvider = <TData, TValue>({
  columns,
  data,
  floatingBarVariant,
  children,
}: DataTableProviderProps<TData, TValue>) => {
  const [tableState, dispatch] = useReducer(tableReducer, {
    floatingBarVariant,
    sorting: [],
    rowSelection: {},
    filters: [],
  });

  // 행 선택 상태에 따른 자동 바 노출 로직 (useEffect)
  useEffect(() => {
    const selectedCount = Object.keys(tableState.rowSelection).length;

    if (selectedCount > 0) {
      // 예: 특정 조건(props 등)에 따라 어떤 바를 띄울지 결정
      dispatch({ type: 'SET_BAR_VARIANT', payload: floatingBarVariant });
    } else {
      dispatch({ type: 'SET_BAR_VARIANT', payload: 'NONE' });
    }
  }, [floatingBarVariant, tableState.rowSelection]);

  const table = useReactTable({
    data,
    columns,
    state: {
      sorting: tableState.sorting,
      rowSelection: tableState.rowSelection,
    },
    // 중요: 핸들러를 dispatch와 연결
    onSortingChange: (updater) =>
      dispatch({ type: 'SET_SORTING', payload: updater }),
    onRowSelectionChange: (updater) =>
      dispatch({ type: 'SET_ROW_SELECTION', payload: updater }),
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    manualFiltering: false, // 클라이언트 사이드 필터링 사용 시
  });

  return (
    <DataTableContext.Provider value={{ table, tableState, dispatch }}>
      {children}
    </DataTableContext.Provider>
  );
};

export default DataTableProvider;
