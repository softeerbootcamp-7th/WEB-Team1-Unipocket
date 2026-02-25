import { useParams } from '@tanstack/react-router';

import ImportToFolderBar from '@/components/data-table/bars/import/ImportToFolderBar';
import { expenseColumns } from '@/components/data-table/columns/expenseColumns';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import { getExpenseGroupKey } from '@/components/data-table/utils/grouping';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

import type { Expense } from '@/api/expenses/type';
import { useGetTravelDetailQuery } from '@/api/travels/query';
import { formatToISODateTime, parseStringToDate } from '@/lib/utils';

interface ImportExpenseTableProps {
  onClose: () => void;
}

const ImportExpenseTable = ({ onClose }: ImportExpenseTableProps) => {
  const { travelId } = useParams({ from: '/_app/travel/$travelId' });
  const { data: travel } = useGetTravelDetailQuery(travelId);

  // 1. 문자열을 Date 객체로 변환
  const parsedStartDate = parseStringToDate(travel?.startDate || '');
  const parsedEndDate = parseStringToDate(travel?.endDate || '');

  // 2. Date 객체를 API가 원하는 ISO 포맷(시작일 00시, 종료일 23시 59분)으로 변환
  const formattedStartDate = formatToISODateTime(parsedStartDate, false);
  const formattedEndDate = formatToISODateTime(parsedEndDate, true);

  // 3. 변환된 값을 초기 필터로 주입
  const { data, filter, updateFilter, totalPages } = useFilteredExpenses({
    startDate: formattedStartDate,
    endDate: formattedEndDate,
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
      totalPages={totalPages}
      columns={expenseColumns}
      groupBy={groupBy}
      groupDisplay={groupDisplay}
    >
      <ImportToFolderBar onSuccess={onClose} />
    </BaseExpenseTable>
  );
};

export default ImportExpenseTable;
