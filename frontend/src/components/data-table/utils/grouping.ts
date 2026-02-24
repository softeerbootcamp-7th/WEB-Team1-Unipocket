import type { Expense } from '@/api/expenses/type';
import type { TempExpense } from '@/api/temporary-expenses/type';

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

export const getExpenseGroupKey = (row: Expense, isAmountSort: boolean) => {
  const dateStr = formatDate(row.occurredAt);
  return isAmountSort ? `${dateStr}__${row.baseCurrencyAmount}` : dateStr;
};

/** TempExpense 전용 그룹핑 키 생성 함수 */
export const getTempExpenseGroupKey = (row: TempExpense) => {
  if (!row.occurredAt) return 'UNCLASSIFIED';
  return formatDate(row.occurredAt);
};
