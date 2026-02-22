import { useState, useTransition } from 'react';

import Button from '@/components/common/Button';
import SelectionActionBar from '@/components/data-table/bars/SelectionActionBar';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';
import TableSidePanel from '@/components/side-panel/TableSidePanel';

import { useGetExpensesQuery } from '@/api/expenses/query';
import type { ExpenseSearchFilter } from '@/api/expenses/type';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface ExpenseTableProps {
  onOpenBottomSheet: () => void;
}

const ExpenseTable = ({ onOpenBottomSheet }: ExpenseTableProps) => {
  const travelId = 1; // 임시 @지원이 여행 PR에서 이어서해줄거임!
  const { accountBookId } = useRequiredAccountBook();
  const [isPending, startTransition] = useTransition();

  const [filter, setFilter] = useState<ExpenseSearchFilter>({
    page: 0,
    size: 50,
    travelId: travelId,
  });

  const updateFilter = (newFilter: Partial<ExpenseSearchFilter>) => {
    startTransition(() => {
      setFilter((prev) => ({ ...prev, ...newFilter, page: 0 }));
    });
  };

  const { data } = useGetExpensesQuery(accountBookId, filter);

  return (
    <BaseExpenseTable
      data={data.expenses}
      isPending={isPending}
      filter={filter}
      updateFilter={updateFilter}
      blankFallbackText="여행 지출 내역을 추가해주세요"
      filterActions={
        // 💡 여행 상세 표만의 우측 상단 버튼
        <Button variant="solid" size="md" onClick={onOpenBottomSheet}>
          지출 내역 불러오기
        </Button>
      }
    >
      {/* 💡 여행 상세 표의 하단 요소들 */}
      <SelectionActionBar />
      <TextCellEditor />
      <CategoryCellEditor />
      <TableSidePanel />
    </BaseExpenseTable>
  );
};

export default ExpenseTable;
