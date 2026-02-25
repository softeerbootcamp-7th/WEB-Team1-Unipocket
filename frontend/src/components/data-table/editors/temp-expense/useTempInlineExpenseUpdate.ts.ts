import { useDataTable } from '@/components/data-table/context';

import { useBulkUpdateTempExpensesMutation } from '@/api/temporary-expenses/query';
import type { TempExpense } from '@/api/temporary-expenses/type';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

export const useTempInlineExpenseUpdate = () => {
  const { table } = useDataTable();
  const { accountBookId } = useRequiredAccountBook();
  const { mutate: updateTempExpense } = useBulkUpdateTempExpensesMutation();

  const updateInline = <K extends keyof TempExpense>(
    rowId: string,
    field: K,
    value: TempExpense[K] | null,
    additionalFields?: Partial<TempExpense>,
  ) => {
    const row = table.getRow(rowId);
    if (!row) return;

    const original = row.original as TempExpense;

    // 단일 아이템 배열 생성 (PATCH 스펙)
    const payload = [
      {
        tempExpenseId: original.tempExpenseId,
        [field]: value,
        ...additionalFields,
      },
    ];

    updateTempExpense({
      accountBookId,
      metaId: original.tempExpenseMetaId,
      fileId: original.fileId,
      data: { items: payload },
    });
  };

  return { updateInline };
};
