import { useState, useTransition } from 'react';

import ImportToFolderBar from '@/components/data-table/bars/ImportToFolderBar';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

import { useGetExpensesQuery } from '@/api/expenses/query';
import type { ExpenseSearchFilter } from '@/api/expenses/type';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const ImportExpenseTable = () => {
  const { accountBookId } = useRequiredAccountBook();
  const [isPending, startTransition] = useTransition();

  const [filter, setFilter] = useState<ExpenseSearchFilter>({
    page: 0,
    size: 50,
  });

  const updateFilter = (newFilter: Partial<ExpenseSearchFilter>) => {
    startTransition(() => {
      setFilter((prev) => ({ ...prev, ...newFilter, page: 0 }));
    });
  };

  const { data } = useGetExpensesQuery(accountBookId, filter);

  return (
    <BaseExpenseTable
      data={data.expenses}
      isPending={isPending}
      filter={filter}
      updateFilter={updateFilter}
      blankFallbackText="여행 지출 내역을 추가해주세요"
    >
      {/* 💡 바텀 시트 전용 하단 액션바 */}
      <ImportToFolderBar />
    </BaseExpenseTable>
  );
};

export default ImportExpenseTable;
