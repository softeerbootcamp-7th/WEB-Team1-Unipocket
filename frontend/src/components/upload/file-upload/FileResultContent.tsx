import type { ColumnDef } from '@tanstack/react-table';

import { DataTable } from '@/components/data-table/DataTable';
import DataTableProvider from '@/components/data-table/DataTableProvider';

import { CATEGORIES } from '@/types/category';

import type {
  TempExpense,
  TempExpenseFile,
} from '@/api/temporary-expenses/type';

const tempExpenseColumns: ColumnDef<TempExpense>[] = [
  {
    accessorKey: 'occurredAt',
    header: () => <>날짜</>,
    cell: ({ row }) =>
      new Date(row.original.occurredAt).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      }),
  },
  {
    accessorKey: 'merchantName',
    header: () => <>거래처</>,
  },
  {
    accessorKey: 'category',
    header: () => <>카테고리</>,
    cell: ({ row }) => CATEGORIES[row.original.category]?.name ?? '-',
  },
  {
    id: 'localAmount',
    header: () => <>현지 금액</>,
    cell: ({ row }) =>
      `${row.original.localCurrencyAmount.toLocaleString()} ${row.original.localCountryCode}`,
  },
  {
    id: 'baseAmount',
    header: () => <>원화 금액</>,
    cell: ({ row }) =>
      `${row.original.baseCurrencyAmount.toLocaleString()} ${row.original.baseCountryCode}`,
  },
  {
    accessorKey: 'status',
    header: () => <>상태</>,
  },
];

interface FileResultContentProps {
  file: TempExpenseFile | null;
}

const FileResultContent = ({ file }: FileResultContentProps) => {
  if (!file) {
    return (
      <div className="flex h-full items-center justify-center">
        <span className="body1-normal-medium text-label-alternative">
          파일을 불러오는 중이에요.
        </span>
      </div>
    );
  }

  return (
    <div className="shadow-semantic-subtle h-fit rounded-2xl px-2 py-4">
      <DataTableProvider columns={tempExpenseColumns} data={file.expenses}>
        <DataTable<TempExpense>
          enableGroupSelection={false}
          groupBy={(row) =>
            new Date(row.occurredAt).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
            })
          }
          blankFallbackText="지출 내역이 없습니다"
        />
      </DataTableProvider>
    </div>
  );
};

export default FileResultContent;
