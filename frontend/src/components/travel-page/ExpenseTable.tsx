import { useFilteredExpenses } from '@/hooks/useFilteredExpenses';

import Button from '@/components/common/Button';
import SelectionActionBar from '@/components/data-table/bars/SelectionActionBar';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';
import TableSidePanel from '@/components/side-panel/TableSidePanel';

interface ExpenseTableProps {
  onOpenBottomSheet: () => void;
}

const ExpenseTable = ({ onOpenBottomSheet }: ExpenseTableProps) => {
  const travelId = 1; // 임시 @지원이 여행 PR에서 이어서해줄거임!

  const { data, filter, updateFilter } = useFilteredExpenses({ travelId });

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
    >
      <SelectionActionBar />
      <TextCellEditor />
      <CategoryCellEditor />
      <TableSidePanel />
    </BaseExpenseTable>
  );
};

export default ExpenseTable;
