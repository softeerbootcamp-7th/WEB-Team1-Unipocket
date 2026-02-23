import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { useInlineExpenseUpdate } from '@/components/data-table/editors/useInlineExpenseUpdate';
import { TravelSelectorContent } from '@/components/data-table/selectors/TravelSelectorContent';
import type { ActiveCellState } from '@/components/data-table/type';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

import type { Expense } from '@/api/expenses/type';
import { NONE_TRAVEL } from '@/constants/column';

const TravelCellEditor = () => {
  const { tableState } = useDataTable();
  const { travelCell } = tableState;

  if (!travelCell) return null;

  return (
    <TravelCellEditorContent
      key={`${travelCell.rowId}-${travelCell.columnId}`}
      travelCell={travelCell}
    />
  );
};

const TravelCellEditorContent = ({
  travelCell,
}: {
  travelCell: ActiveCellState;
}) => {
  const { dispatch, table } = useDataTable();
  const { updateInline } = useInlineExpenseUpdate();

  const original = table.getRow(travelCell.rowId)?.original as Expense;
  const initialTravelId = original?.travel?.travelId ?? NONE_TRAVEL;

  const closeEditor = () =>
    dispatch({ type: 'SET_TRAVEL_CELL', payload: null });

  const handleSelect = (travelId: number | string) => {
    if (travelId !== initialTravelId) {
      const targetTravelId = travelId === NONE_TRAVEL ? null : Number(travelId);
      updateInline(travelCell.rowId, 'travelId', targetTravelId);
    }
    closeEditor();
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && closeEditor()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={travelCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>

      <TravelSelectorContent
        initialTravelId={initialTravelId}
        onTravelSelect={handleSelect}
        onInteractOutside={closeEditor}
      />
    </Popover>
  );
};

export default TravelCellEditor;
