import AmountCellEditor from '@/components/data-table/editors/common/AmountCellEditor';
import { useTempInlineExpenseUpdate } from '@/components/data-table/editors/temp-expense/useTempInlineExpenseUpdate.ts';

import type { TempExpense } from '@/api/temporary-expenses/type';
import type { CurrencyCode } from '@/data/country/currencyCode';

const TempAmountCellEditor = () => {
  const { updateInline } = useTempInlineExpenseUpdate();

  const getCurrencyCode = (
    original: TempExpense,
    isLocal: boolean,
  ): CurrencyCode | null => {
    return isLocal ? original.localCountryCode : original.baseCountryCode;
  };

  return (
    <AmountCellEditor<TempExpense> // 명시적으로 제네릭 주입
      onUpdate={(rowId, field, value, oppositeField) =>
        updateInline(rowId, field, value, { [oppositeField]: null })
      }
      getCurrencyCode={getCurrencyCode}
    />
  );
};

export default TempAmountCellEditor;
