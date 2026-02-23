import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { useInlineExpenseUpdate } from '@/components/data-table/editors/useInlineExpenseUpdate';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';
import type { ActiveCellState } from '@/components/data-table/type';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

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
  const { dispatch } = useDataTable();
  const { updateInline } = useInlineExpenseUpdate();
  const { data: cards = [] } = useGetCardsQuery();

  const closeEditor = () =>
    dispatch({ type: 'SET_METHOD_CELL', payload: null });

  const handleSelect = (cardNumber: string) => {
    if (cardNumber !== methodCell.value) {
      // cardNumber를 기반으로 원본 객체에서 userCardId를 찾아 업데이트 요청
      const card = cards.find((c) => c.cardNumber === cardNumber);
      if (card && 'userCardId' in card) {
        updateInline(methodCell.rowId, 'userCardId', card.userCardId as number);
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
        initialCardNumber={methodCell.value as string}
        onMethodSelect={handleSelect}
        onInteractOutside={closeEditor}
      />
    </Popover>
  );
};

export default MethodCellEditor;
