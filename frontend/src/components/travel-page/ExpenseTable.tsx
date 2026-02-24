import { useParams } from '@tanstack/react-router';

import Button from '@/components/common/Button';
import UpdateActionBar from '@/components/data-table/bars/update/UpdateActionBar';
import { expenseColumns } from '@/components/data-table/columns/expenseColumns';
import AmountCellEditor from '@/components/data-table/editors/AmountCellEditor';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import MethodCellEditor from '@/components/data-table/editors/MethodCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import TravelCellEditor from '@/components/data-table/editors/TravelCellEditor';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

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
    >
      <UpdateActionBar />
      <TextCellEditor />
      <CategoryCellEditor />
      <AmountCellEditor />
      <MethodCellEditor />
      <TravelCellEditor />
    </BaseExpenseTable>
  );
};

export default ExpenseTable;
