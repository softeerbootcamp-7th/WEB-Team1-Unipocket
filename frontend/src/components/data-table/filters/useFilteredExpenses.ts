import { useCallback, useEffect, useState, useTransition } from 'react';

import { useGetExpensesQuery } from '@/api/expenses/query';
import type { ExpenseSearchFilter } from '@/api/expenses/type';
import { DEFAULT_PAGE_SIZE } from '@/constants/column';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

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

  const { data } = useGetExpensesQuery(accountBookId, filter);

  const totalCount = data?.totalCount || 0;
  const size = filter.size || DEFAULT_PAGE_SIZE;
  const totalPages = Math.ceil(totalCount / size);

  //  useEffect 안에서 안전하게 호출할 수 있도록 useCallback으로 메모이제이션
  const updateFilter = useCallback(
    (newFilter: Partial<ExpenseSearchFilter>) => {
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
    },
    [],
  );

  // 현재 페이지가 유효한 페이지 범위를 넘어섰을 때 방어하는 로직 추가
  useEffect(() => {
    // 데이터가 존재하고(totalPages > 0), 현재 페이지가 전체 페이지 수를 벗어난 경우
    if (
      totalPages > 0 &&
      filter.page !== undefined &&
      filter.page >= totalPages
    ) {
      // 가장 마지막에 있는 유효한 페이지로 강제 이동
      updateFilter({ page: totalPages - 1 });
    }
  }, [totalPages, filter.page, updateFilter]);

  return {
    data: data?.expenses || [],
    totalCount,
    totalPages,
    filter,
    updateFilter,
  };
};
