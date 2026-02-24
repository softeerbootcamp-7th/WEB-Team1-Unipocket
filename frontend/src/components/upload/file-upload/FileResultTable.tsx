import { useMemo } from 'react';

import { getTempExpenseColumns } from '@/components/data-table/columns/tempExpenseColumns';
import { getTempExpenseGroupKey } from '@/components/data-table/utils/grouping';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

import type { TempExpense } from '@/api/temporary-expenses/type';
import { DEFAULT_PAGE_SIZE } from '@/constants/column';

interface FileResultTableProps {
  data: TempExpense[];
}

const FileResultTable = ({ data }: FileResultTableProps) => {
  const dummyFilter = { page: 0 };
  const dummyUpdateFilter = () => {};
  const totalPages = Math.ceil(data.length / DEFAULT_PAGE_SIZE);

  const groupBy = getTempExpenseGroupKey;

  const groupDisplay = (groupKey: string) => {
    if (groupKey === 'UNCLASSIFIED')
      return '날짜 미분류 -> 미지정 시 오늘 날짜로 들어갑니다';
    return groupKey;
  };

  const columns = useMemo(() => getTempExpenseColumns(), []);

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

export default FileResultTable;
