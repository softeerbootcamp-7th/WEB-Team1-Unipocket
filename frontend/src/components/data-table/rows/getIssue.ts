import type { RowIssueType } from '@/components/expense/BaseExpenseTable';

import type { TempExpense } from '@/api/temporary-expenses/type';

export const getRowIssue = (
  row: TempExpense,
  startDate: string,
  endDate: string,
): RowIssueType => {
  if (row.status === 'INCOMPLETE') return 'incomplete'; // 미완성 상태

  if (!row.occurredAt) return 'impossible'; // 날짜 미분류
  const date = row.occurredAt.split('T')[0];
  if (date < startDate || date > endDate) return 'impossible'; // 가계부 기간 외

  return false;
};
