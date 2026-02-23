import type { RowSelectionState, Updater } from '@tanstack/react-table';

type SelectionModeState = 'MANAGEMENT' | 'ADD_TO_FOLDER';

type ActiveCellState = {
  rowId: string;
  columnId: string;
  rect: DOMRect;
  value: unknown;
};

type ActiveRowState = {
  rowId: string;
  value: unknown;
};

type TableUIState = {
  selectionMode: SelectionModeState | null;
  rowSelection: RowSelectionState;
  textCell: ActiveCellState | null;
  categoryCell: ActiveCellState | null;
  amountCell: ActiveCellState | null;
  methodCell: ActiveCellState | null;
  travelCell: ActiveCellState | null;
  activeRow: ActiveRowState | null;
};

type TableUIAction =
  | { type: 'SET_SELECTION_MODE'; payload: SelectionModeState | null }
  | { type: 'SET_ROW_SELECTION'; payload: Updater<RowSelectionState> }
  | { type: 'SET_TEXT_CELL'; payload: ActiveCellState | null }
  | { type: 'SET_CATEGORY_CELL'; payload: ActiveCellState | null }
  | { type: 'SET_AMOUNT_CELL'; payload: ActiveCellState | null }
  | { type: 'SET_METHOD_CELL'; payload: ActiveCellState | null }
  | { type: 'SET_TRAVEL_CELL'; payload: ActiveCellState | null }
  | { type: 'SET_ACTIVE_ROW'; payload: ActiveRowState | null };

export type { ActiveCellState, ActiveRowState, TableUIAction, TableUIState };
