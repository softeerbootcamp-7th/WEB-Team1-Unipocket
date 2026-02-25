import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';
import type { ActiveCellState } from '@/components/data-table/type';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

interface MethodCellEditorProps<TData> {
  getInitialCardNumber: (original: TData) => string;
  onUpdate: (
    rowId: string,
    selectedCardNumber: string,
    original: TData,
  ) => void;
}

const MethodCellEditor = <TData,>({
  getInitialCardNumber,
  onUpdate,
}: MethodCellEditorProps<TData>) => {
  const { tableState } = useDataTable();
  const { methodCell } = tableState;

  if (!methodCell) return null;

  return (
    <MethodCellEditorContent
      key={`${methodCell.rowId}-${methodCell.columnId}`}
      methodCell={methodCell}
      getInitialCardNumber={getInitialCardNumber}
      onUpdate={onUpdate}
    />
  );
};

const MethodCellEditorContent = <TData,>({
  methodCell,
  getInitialCardNumber,
  onUpdate,
}: {
  methodCell: ActiveCellState;
} & MethodCellEditorProps<TData>) => {
  const { dispatch, table } = useDataTable();
  const original = table.getRow(methodCell.rowId)?.original as TData;
  const initialCardNumber = getInitialCardNumber(original);

  const closeEditor = () =>
    dispatch({ type: 'SET_METHOD_CELL', payload: null });

  const handleSelect = (cardNumber: string) => {
    if (cardNumber !== initialCardNumber) {
      onUpdate(methodCell.rowId, cardNumber, original);
    }
    closeEditor();
  };
  return (
    <Popover open={true} onOpenChange={(open) => !open && closeEditor()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={methodCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>

      <MethodSelectorContent
        initialCardNumber={initialCardNumber}
        onMethodSelect={handleSelect}
        onInteractOutside={closeEditor}
      />
    </Popover>
  );
};

export default MethodCellEditor;
