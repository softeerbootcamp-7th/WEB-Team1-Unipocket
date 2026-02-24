import { useMemo } from 'react';

import TempUpdateActionBar from '@/components/data-table/bars/update/TempUpdateActionBar'; // 새로 만든 바
import { getTempExpenseColumns } from '@/components/data-table/columns/tempExpenseColumns';
import AmountCellEditor from '@/components/data-table/editors/AmountCellEditor';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import MethodCellEditor from '@/components/data-table/editors/MethodCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import { getTempExpenseGroupKey } from '@/components/data-table/utils/grouping';
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
  const groupBy = getTempExpenseGroupKey;

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
        <TempUpdateActionBar />

        <TextCellEditor />
        <CategoryCellEditor />
        <AmountCellEditor />
        <MethodCellEditor />
      </BaseExpenseTable>
    </div>
  );
};

export default ImageResultTable;
