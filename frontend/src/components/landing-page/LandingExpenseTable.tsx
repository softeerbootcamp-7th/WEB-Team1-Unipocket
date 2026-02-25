import { useMemo } from 'react';
import type { ColumnDef } from '@tanstack/react-table';

import { getTempExpenseColumns } from '@/components/data-table/columns/tempExpenseColumns';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

import type { TempExpense } from '@/api/temporary-expenses/type';
import { DEFAULT_PAGE_SIZE } from '@/constants/column';

const paymentMethodColumn: ColumnDef<TempExpense> = {
  id: 'paymentMethod',
  size: 20,
  header: () => <>결제 수단</>,
  cell: ({ row }) => {
    const digits = row.original.cardLastFourDigits;
    if (!digits) return <>현금</>;
    return <>카드 ({digits})</>;
  },
};

const groupBy = (row: TempExpense) => {
  if (!row.occurredAt) return 'UNCLASSIFIED';
  return new Date(row.occurredAt).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const groupDisplay = (groupKey: string) => {
  if (groupKey === 'UNCLASSIFIED') return '날짜 미분류';
  return groupKey;
};

interface LandingExpenseTableProps {
  data: TempExpense[];
}

const LandingExpenseTable = ({ data }: LandingExpenseTableProps) => {
  const dummyFilter = { page: 0 };
  const dummyUpdateFilter = () => {};
  const totalPages = Math.ceil(data.length / DEFAULT_PAGE_SIZE);
  const columns = useMemo(
    () => [
      ...getTempExpenseColumns({
        showBaseCurrencyAmount: false,
        showPaymentMethod: false,
      }),
      paymentMethodColumn,
    ],
    [],
  );

  return (
    <div className="bg-background-normal relative flex min-h-0 flex-1 flex-col px-2">
      <BaseExpenseTable
        data={data}
        columns={columns}
        filter={dummyFilter}
        totalPages={totalPages}
        updateFilter={dummyUpdateFilter}
        hideFilters={true}
        groupBy={groupBy}
        groupDisplay={groupDisplay}
      />
    </div>
  );
};

export default LandingExpenseTable;
