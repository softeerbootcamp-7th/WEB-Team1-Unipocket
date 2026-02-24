import MethodCellEditor from '@/components/data-table/editors/common/MethodCellEditor';
import {
  getCardNumberFromExpense,
  resolveUserCardId,
} from '@/components/data-table/util';

import type { Expense } from '@/api/expenses/type';
import { useGetCardsQuery } from '@/api/users/query';

import { useInlineExpenseUpdate } from './useInlineExpenseUpdate';

const ExpenseMethodCellEditor = () => {
  const { updateInline } = useInlineExpenseUpdate();
  const { data: cards = [] } = useGetCardsQuery();

  const getInitialCardNumber = (original: Expense) => {
    return getCardNumberFromExpense(original, cards);
  };

  const handleUpdate = (rowId: string, cardNumber: string) => {
    const userCardId = resolveUserCardId(cardNumber, cards);

    if (userCardId !== undefined) {
      updateInline(rowId, 'userCardId', userCardId);
    }
  };

  return (
    <MethodCellEditor<Expense>
      getInitialCardNumber={getInitialCardNumber}
      onUpdate={handleUpdate}
    />
  );
};

export default ExpenseMethodCellEditor;
