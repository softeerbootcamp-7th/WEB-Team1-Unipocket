import type { RowSelectionState, Updater } from '@tanstack/react-table';

type TableSelectionMode = 'MANAGEMENT' | 'ADD_TO_FOLDER';

type ActiveCell<TValue> = {
  rowId: string;
  columnId: string;
  rect: DOMRect;
  value: TValue;
};

type TableUIState<TValue> = {
  selectionMode: TableSelectionMode | null;
  rowSelection: RowSelectionState;
  activeCell: ActiveCell<TValue> | null;
};

type TableUIAction<TValue> =
  | { type: 'SET_SELECTION_MODE'; payload: TableSelectionMode | null }
  | { type: 'SET_ROW_SELECTION'; payload: Updater<RowSelectionState> }
  | { type: 'SET_ACTIVE_CELL'; payload: ActiveCell<TValue> | null };

export type { ActiveCell, TableUIAction, TableUIState };
