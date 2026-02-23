import { useDataTable } from '@/components/data-table/context';

import { useUpdateExpenseMutation } from '@/api/expenses/query';
import type { Expense, UpdateExpenseRequest } from '@/api/expenses/type';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

export const useInlineExpenseUpdate = () => {
  const { accountBookId } = useRequiredAccountBook();
  const { mutate } = useUpdateExpenseMutation();
  const { table } = useDataTable();

  const updateInline = <K extends keyof UpdateExpenseRequest>(
    rowId: string,
    field: K,
    value: NonNullable<UpdateExpenseRequest[K]>,
  ) => {
    const row = table.getRow(rowId);
    if (!row) return;

    const original = row.original as Expense;

    const payload: UpdateExpenseRequest = {
      merchantName: original.merchantName,
      category: original.category,
      occurredAt: original.occurredAt,
      localCurrencyAmount: original.localCurrencyAmount,
      localCurrencyCode: original.localCurrencyCode,
      baseCurrencyAmount: original.baseCurrencyAmount,
      memo: original.memo || '',
      travelId: original.travel?.travelId || null,
      userCardId: original.paymentMethod.isCash ? null : 0,
      [field]: value, // 변경된 필드만 덮어쓰기
    };

    mutate({
      accountBookId,
      expenseId: original.expenseId,
      data: payload,
    });
  };

  return { updateInline };
};
