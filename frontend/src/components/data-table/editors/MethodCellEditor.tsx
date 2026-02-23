import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { useInlineExpenseUpdate } from '@/components/data-table/editors/useInlineExpenseUpdate';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';
import type { ActiveCellState } from '@/components/data-table/type';
import {
  getCardNumberFromExpense,
  resolveUserCardId,
} from '@/components/data-table/util';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

import type { Expense } from '@/api/expenses/type';
import { useGetCardsQuery } from '@/api/users/query';

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
  const { dispatch, table } = useDataTable();
  const { updateInline } = useInlineExpenseUpdate();
  const { data: cards = [] } = useGetCardsQuery();

  const original = table.getRow(methodCell.rowId)?.original as Expense;

  const initialCardNumber = getCardNumberFromExpense(original, cards);

  const closeEditor = () =>
    dispatch({ type: 'SET_METHOD_CELL', payload: null });

  const handleSelect = (cardNumber: string) => {
    if (cardNumber !== initialCardNumber) {
      const userCardId = resolveUserCardId(cardNumber, cards);

      if (userCardId !== undefined) {
        updateInline(methodCell.rowId, 'userCardId', userCardId);
      }
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
