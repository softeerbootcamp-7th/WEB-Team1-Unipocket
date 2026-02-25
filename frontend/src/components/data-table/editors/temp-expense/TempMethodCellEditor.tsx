import MethodCellEditor from '@/components/data-table/editors/common/MethodCellEditor';
import { useTempInlineExpenseUpdate } from '@/components/data-table/editors/temp-expense/useTempInlineExpenseUpdate.ts';

import type { TempExpense } from '@/api/temporary-expenses/type';
import { CASH } from '@/constants/column';

const TempMethodCellEditor = () => {
  const { updateInline } = useTempInlineExpenseUpdate();

  const getInitialCardNumber = (original: TempExpense) => {
    return !original.cardLastFourDigits || original.cardLastFourDigits === ''
      ? CASH
      : original.cardLastFourDigits;
  };

  const handleUpdate = (rowId: string, cardNumber: string) => {
    const newValue = cardNumber === CASH ? null : cardNumber;
    updateInline(rowId, 'cardLastFourDigits', newValue);
  };

  return (
    <MethodCellEditor<TempExpense>
      getInitialCardNumber={getInitialCardNumber}
      onUpdate={handleUpdate}
    />
  );
};

export default TempMethodCellEditor;
