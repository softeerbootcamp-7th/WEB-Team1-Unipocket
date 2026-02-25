import AmountCellEditor from '@/components/data-table/editors/common/AmountCellEditor';

import type { Expense } from '@/api/expenses/type';
import type { CurrencyCode } from '@/data/country/currencyCode';

import { useInlineExpenseUpdate } from './useInlineExpenseUpdate';

const ExpenseAmountCellEditor = () => {
  const { updateInline } = useInlineExpenseUpdate();

  const getCurrencyCode = (
    original: Expense,
    isLocal: boolean,
  ): CurrencyCode | null => {
    return isLocal ? original.localCurrencyCode : original.baseCurrencyCode;
  };

  return (
    <AmountCellEditor<Expense>
      onUpdate={(rowId, field, value, oppositeField) =>
        updateInline(rowId, field, value, { [oppositeField]: null })
      }
      getCurrencyCode={getCurrencyCode}
    />
  );
};

export default ExpenseAmountCellEditor;
