import type { RowSelectionState, Updater } from '@tanstack/react-table';

type TableSelectionMode = 'MANAGEMENT' | 'ADD_TO_FOLDER';

type ActiveCell = {
  rowId: string;
  columnId: string;
  rect: DOMRect;
  value: unknown;
};

type ActiveRow = {
  rowId: string;
  value: unknown;
};

type TableUIState = {
  selectionMode: TableSelectionMode | null;
  rowSelection: RowSelectionState;
  activeCell: ActiveCell | null;
  activeRow: ActiveRow | null;
};

type TableUIAction =
  | { type: 'SET_SELECTION_MODE'; payload: TableSelectionMode | null }
  | { type: 'SET_ROW_SELECTION'; payload: Updater<RowSelectionState> }
  | { type: 'SET_ACTIVE_CELL'; payload: ActiveCell | null }
  | { type: 'SET_ACTIVE_ROW'; payload: ActiveRow | null };

export type { ActiveCell, ActiveRow, TableUIAction, TableUIState };
