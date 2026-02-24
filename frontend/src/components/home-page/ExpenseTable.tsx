import UpdateActionBar from '@/components/data-table/bars/update/UpdateActionBar';
import { expenseColumns } from '@/components/data-table/columns/expenseColumns';
import AmountCellEditor from '@/components/data-table/editors/AmountCellEditor';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import MethodCellEditor from '@/components/data-table/editors/MethodCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import TravelCellEditor from '@/components/data-table/editors/TravelCellEditor';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import { getExpenseGroupKey } from '@/components/data-table/utils/grouping';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';
import UploadMenu from '@/components/upload/UploadMenu';

import type { Expense } from '@/api/expenses/type';

const ExpenseTable = () => {
  const { data, totalPages, filter, updateFilter } = useFilteredExpenses();

  const currentSort = filter.sort?.[0] || 'occurredAt,desc';
  const isAmountSort = currentSort.startsWith('baseCurrencyAmount');

  const groupBy = (row: Expense) => getExpenseGroupKey(row, isAmountSort);

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
        <UpdateActionBar />
        <TextCellEditor />
        <CategoryCellEditor />
        <AmountCellEditor />
        <MethodCellEditor />
        <TravelCellEditor />
      </BaseExpenseTable>
    </div>
  );
};

export default ExpenseTable;
