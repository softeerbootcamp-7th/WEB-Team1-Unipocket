import { useDataTable } from '@/components/data-table/context';
import AmountCellEditor from '@/components/data-table/editors/common/AmountCellEditor';
import { useTempInlineExpenseUpdate } from '@/components/data-table/editors/temp-expense/useTempInlineExpenseUpdate.ts';

import type { TempExpense } from '@/api/temporary-expenses/type';
import type { CurrencyCode } from '@/data/country/currencyCode';

const TempAmountCellEditor = () => {
  const { updateInline } = useTempInlineExpenseUpdate();
  const { table } = useDataTable();

  const getCurrencyCode = (
    original: TempExpense,
    isLocal: boolean,
  ): CurrencyCode | null => {
    return isLocal ? original.localCountryCode : original.baseCountryCode;
  };

  return (
    <AmountCellEditor<TempExpense> // 명시적으로 제네릭 주입
      onUpdate={(rowId, field, value, oppositeField) => {
        const row = table.getRow(rowId);
        const original = row?.original as TempExpense | undefined;

        // localCountryCode === baseCountryCode 이면 두 금액 모두 동일하게 맞춰야 함
        const isSameCurrency =
          original?.localCountryCode !== null &&
          original?.localCountryCode === original?.baseCountryCode;

        updateInline(rowId, field, value, {
          [oppositeField]: isSameCurrency ? value : null,
        });
      }}
      getCurrencyCode={getCurrencyCode}
    />
  );
};

export default TempAmountCellEditor;
