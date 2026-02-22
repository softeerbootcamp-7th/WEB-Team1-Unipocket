import { useState, useTransition } from 'react';

import { useGetExpensesQuery } from '@/api/expenses/query';
import type { ExpenseSearchFilter } from '@/api/expenses/type';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

export const useFilteredExpenses = (
  initialFilter?: Partial<ExpenseSearchFilter>,
) => {
  const { accountBookId } = useRequiredAccountBook();
  const [, startTransition] = useTransition();

  const [filter, setFilter] = useState<ExpenseSearchFilter>({
    page: 0,
    size: 50,
    ...initialFilter,
  });

  const updateFilter = (newFilter: Partial<ExpenseSearchFilter>) => {
    startTransition(() => {
      setFilter((prev) => ({ ...prev, ...newFilter, page: 0 }));
    });
  };

  const { data } = useGetExpensesQuery(accountBookId, filter);

  return {
    data: data.expenses,
    filter,
    updateFilter,
  };
};
