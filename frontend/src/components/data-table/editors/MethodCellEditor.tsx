import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';
import type { ActiveCellState } from '@/components/data-table/type';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

const MethodCellEditor = () => {
  const { tableState } = useDataTable();
  const { methodCell } = tableState;

  if (!methodCell) return null;

  return (
    <MethodCellEditorContent
      key={`${methodCell.rowId}-${methodCell.columnId}`}
      methodCell={methodCell}
    />
  );
};

const MethodCellEditorContent = ({
  methodCell,
}: {
  methodCell: ActiveCellState;
}) => {
  const { dispatch } = useDataTable();

  const handleSave = () => {
    dispatch({ type: 'SET_METHOD_CELL', payload: null });
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && handleSave()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={methodCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>

      <MethodSelectorContent
        initialCardNumber={methodCell.value as string}
        onMethodSelect={() => handleSave()}
        onInteractOutside={handleSave}
      />
    </Popover>
  );
};

export default MethodCellEditor;
