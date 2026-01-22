import type {
  RowSelectionState,
  SortingState,
  Updater,
} from '@tanstack/react-table';

type FloatingBarVariant = 'MANAGEMENT' | 'ADD_TO_LEDGER' | 'NONE';

type FilterConfig = Record<string, string[]>;

type TableUIState = {
  floatingBarVariant: FloatingBarVariant;
  sorting: SortingState;
  rowSelection: RowSelectionState; // {} 타입 대신 정확한 타입 사용
  filters?: FilterConfig[];
  // floating바 말고 다른 필터링도 이 상태에서 관리해야하나...? 확장성을 고려한다면 어떤 상태라고 해야하지?
};

type TableUIAction =
  | { type: 'SET_BAR_VARIANT'; payload: FloatingBarVariant }
  | { type: 'SET_FILTERS'; payload: FilterConfig } // 필터 부분 업데이트
  | { type: 'RESET_FILTERS'; payload: FilterConfig } // 필터 초기화
  | { type: 'TOGGLE_ROW_SELECTION'; payload: string }
  | { type: 'SET_SORTING'; payload: Updater<SortingState> }
  | { type: 'SET_ROW_SELECTION'; payload: Updater<RowSelectionState> };

export type { FilterConfig, FloatingBarVariant, TableUIAction, TableUIState };
