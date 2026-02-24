import ImportToFolderBar from '@/components/data-table/bars/import/ImportToFolderBar';
import { expenseColumns } from '@/components/data-table/columns/expenseColumns';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import { getExpenseGroupKey } from '@/components/data-table/utils/grouping';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

import type { Expense } from '@/api/expenses/type';

const ImportExpenseTable = () => {
  const { data, filter, updateFilter, totalPages } = useFilteredExpenses();

  const currentSort = filter.sort?.[0] || 'occurredAt,desc';
  const isAmountSort = currentSort.startsWith('baseCurrencyAmount');

  const groupBy = (row: Expense) => getExpenseGroupKey(row, isAmountSort);

  const groupDisplay = (groupKey: string) => groupKey.split('__')[0];

  return (
    <BaseExpenseTable
      data={data}
      filter={filter}
      updateFilter={updateFilter}
      blankFallbackText="여행 지출 내역을 추가해주세요"
      totalPages={totalPages}
      columns={expenseColumns}
      groupBy={groupBy}
      groupDisplay={groupDisplay}
    >
      <ImportToFolderBar />
    </BaseExpenseTable>
  );
};

export default ImportExpenseTable;
