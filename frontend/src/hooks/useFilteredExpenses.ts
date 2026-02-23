import { useState, useTransition } from 'react';

import { useGetExpensesQuery } from '@/api/expenses/query';
import type { ExpenseSearchFilter } from '@/api/expenses/type';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const DEFAULT_PAGE_SIZE = 50;

export const useFilteredExpenses = (
  initialFilter?: Partial<ExpenseSearchFilter>,
) => {
  const { accountBookId } = useRequiredAccountBook();
  const [, startTransition] = useTransition();

  const [filter, setFilter] = useState<ExpenseSearchFilter>({
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    ...initialFilter,
  });

  const updateFilter = (newFilter: Partial<ExpenseSearchFilter>) => {
    startTransition(() => {
      // 페이지를 이동하는 게 아니라면(예: 카테고리 필터 변경),
      // 새로운 필터 적용 시 1페이지(page: 0)로 돌아가도록 설정
      const isPageChange = newFilter.page !== undefined;
      setFilter((prev) => ({
        ...prev,
        page: isPageChange ? newFilter.page! : 0,
        ...newFilter,
      }));
    });
  };

  const { data } = useGetExpensesQuery(accountBookId, filter);

  const totalCount = data?.totalCount || 0;
  const size = filter.size || DEFAULT_PAGE_SIZE;
  const totalPages = Math.ceil(totalCount / size);

  return {
    data: data?.expenses || [],
    totalCount,
    totalPages,
    filter,
    updateFilter,
  };
};
