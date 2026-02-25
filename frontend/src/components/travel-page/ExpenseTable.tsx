import { useParams } from '@tanstack/react-router';

import Button from '@/components/common/Button';
import UpdateActionBar from '@/components/data-table/bars/update/UpdateActionBar';
import { expenseColumns } from '@/components/data-table/columns/expenseColumns';
import ExpenseAmountCellEditor from '@/components/data-table/editors/expense/ExpenseAmountCellEditor';
import ExpenseCategoryCellEditor from '@/components/data-table/editors/expense/ExpenseCategoryCellEditor';
import ExpenseMethodCellEditor from '@/components/data-table/editors/expense/ExpenseMethodCellEditor';
import ExpenseTextCellEditor from '@/components/data-table/editors/expense/ExpenseTextCellEditor';
import TravelCellEditor from '@/components/data-table/editors/expense/TravelCellEditor';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import { getExpenseGroupKey } from '@/components/data-table/utils/grouping';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

import type { Expense } from '@/api/expenses/type';

interface ExpenseTableProps {
  onOpenBottomSheet: () => void;
}

const ExpenseTable = ({ onOpenBottomSheet }: ExpenseTableProps) => {
  const { travelId: travelIdParam } = useParams({
    from: '/_app/travel/$travelId',
  });
  const travelId = Number(travelIdParam);

  const { data, filter, updateFilter, totalPages } = useFilteredExpenses({
    travelId,
  });

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
      filterActions={
        <Button variant="solid" size="md" onClick={onOpenBottomSheet}>
          지출 내역 불러오기
        </Button>
      }
      totalPages={totalPages}
      columns={expenseColumns}
      groupBy={groupBy}
      groupDisplay={groupDisplay}
    >
      <UpdateActionBar />
      <ExpenseTextCellEditor />
      <ExpenseCategoryCellEditor />
      <ExpenseAmountCellEditor />
      <ExpenseMethodCellEditor />
      <TravelCellEditor />
    </BaseExpenseTable>
  );
};

export default ExpenseTable;
