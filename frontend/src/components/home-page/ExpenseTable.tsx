import { expenseColumns } from '@/components/data-table/columns/expenseColumns';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';
import UploadMenu from '@/components/upload/UploadMenu';

import type { Expense } from '@/api/expenses/type';

const ExpenseTable = () => {
  const { data, totalPages, filter, updateFilter } = useFilteredExpenses();

  const currentSort = filter.sort?.[0] || 'occurredAt,desc';
  const isAmountSort = currentSort.startsWith('baseCurrencyAmount');

  // Expense 전용 그룹핑 로직
  const groupBy = (row: Expense) => {
    const dateStr = new Date(row.occurredAt).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });

    return isAmountSort ? `${dateStr}__${row.baseCurrencyAmount}` : dateStr;
  };

  const groupDisplay = (groupKey: string) => groupKey.split('__')[0];

  return (
    <div className="bg-background-normal relative flex min-h-0 flex-1 flex-col px-2 pt-4">
      <BaseExpenseTable
        data={data}
        columns={expenseColumns}
        filter={filter}
        totalPages={totalPages}
        updateFilter={updateFilter}
        filterActions={<UploadMenu />}
        groupBy={groupBy}
        groupDisplay={groupDisplay}
      >
        {/* ... (기존 children 컴포넌트들) */}
      </BaseExpenseTable>
    </div>
  );
};

export default ExpenseTable;
