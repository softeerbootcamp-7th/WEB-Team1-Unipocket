import { useMemo } from 'react';

import { getTempExpenseColumns } from '@/components/data-table/columns/tempExpenseColumns';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

import type { TempExpense } from '@/api/temporary-expenses/type';
import { DEFAULT_PAGE_SIZE } from '@/constants/column';

interface ImageResultTableProps {
  data: TempExpense[];
}

const ImageResultTable = ({ data }: ImageResultTableProps) => {
  const dummyFilter = { page: 0 };
  const dummyUpdateFilter = () => {};
  const totalPages = Math.ceil(data.length / DEFAULT_PAGE_SIZE);

  // TempExpense 전용 그룹핑 로직
  const groupBy = (row: TempExpense) => {
    if (!row.occurredAt) return 'UNCLASSIFIED'; // null인 경우 특수 키 반환

    return new Date(row.occurredAt).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  const groupDisplay = (groupKey: string) => {
    if (groupKey === 'UNCLASSIFIED')
      return '날짜 미분류 -> 미지정 시 오늘 날짜로 들어갑니다';
    return groupKey;
  };

  const columns = useMemo(
    () => getTempExpenseColumns({ showBaseCurrencyAmount: false }),
    [],
  );

  return (
    <div className="bg-background-normal relative flex min-h-0 flex-1 flex-col px-2 pt-4">
      <BaseExpenseTable
        data={data}
        columns={columns}
        filter={dummyFilter}
        totalPages={totalPages}
        updateFilter={dummyUpdateFilter}
        hideFilters={true}
        groupBy={groupBy}
        groupDisplay={groupDisplay}
      >
        {/* ... (기존 children 컴포넌트들) */}
      </BaseExpenseTable>
    </div>
  );
};

export default ImageResultTable;
