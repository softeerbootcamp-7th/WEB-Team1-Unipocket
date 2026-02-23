import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { useInlineExpenseUpdate } from '@/components/data-table/editors/useInlineExpenseUpdate';
import { TravelSelectorContent } from '@/components/data-table/selectors/TravelSelectorContent';
import type { ActiveCellState } from '@/components/data-table/type';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

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
  const { dispatch } = useDataTable();
  const { updateInline } = useInlineExpenseUpdate();

  const closeEditor = () =>
    dispatch({ type: 'SET_TRAVEL_CELL', payload: null });

  const handleSelect = (travelId: number) => {
    if (travelId !== Number(travelCell.value)) {
      updateInline(travelCell.rowId, 'travelId', travelId);
    }
    closeEditor();
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && closeEditor()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={travelCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>

      <TravelSelectorContent
        initialTravelId={Number(travelCell.value) || 0}
        onTravelSelect={handleSelect}
        onInteractOutside={closeEditor}
      />
    </Popover>
  );
};

export default TravelCellEditor;
