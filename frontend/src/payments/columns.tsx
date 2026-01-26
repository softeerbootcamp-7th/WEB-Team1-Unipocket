import type { ColumnDef } from '@tanstack/react-table';

import { Checkbox } from '@/components/ui/checkbox';

import { DataTableColumnHeader } from '../components/common/data-table/DataTableColumnHeader';
import type { Expense } from './dummy';

export const columns: ColumnDef<Expense>[] = [
  {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={
          table.getIsAllPageRowsSelected() ||
          (table.getIsSomePageRowsSelected() && 'indeterminate')
        }
        onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
        aria-label="Select all"
      />
    ),
    cell: ({ row }) => (
      <Checkbox
        checked={row.getIsSelected()}
        onCheckedChange={(value) => row.toggleSelected(!!value)}
        aria-label="Select row"
      />
    ),
    enableSorting: false,
    enableHiding: false,
  },
  // {
  //   accessorKey: 'date',
  //   header: ({ column }) => (
  //     <DataTableColumnHeader column={column} title="날짜" />
  //   ),
  //   cell: ({ row }) => {
  //     const date = new Date(row.getValue('date'));
  //     return <div>{date.toLocaleDateString('ko-KR')}</div>;
  //   },
  // },
  {
    accessorKey: 'storeName', // 데이터의 storeName 매핑
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="거래처" />
    ),
  },
  {
    id: 'category',
    accessorKey: 'category.name', // 중첩 객체 접근
    header: '카테고리',
  },
  {
    id: 'amount',
    header: '현지 금액',
    cell: ({ row }) => {
      const amount = row.original.amount;
      const currency = row.original.currency;
      return (
        <div className="font-medium">
          {amount.toLocaleString()} {currency}
        </div>
      );
    },
  },
  {
    accessorKey: 'krwAmount',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="기준 금액" />
    ),
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('krwAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <div className="text-right font-semibold">{formatted}</div>;
    },
  },
  {
    accessorKey: 'memo',
    header: '메모',
    cell: ({ row }) => (
      <div className="max-w-[150px] truncate">
        {row.getValue('memo') || '-'}
      </div>
    ),
  },
  {
    id: 'travel',
    accessorKey: 'travel.name', // 여행 이름 매핑
    header: '여행',
  },
  {
    accessorKey: 'hasReceipt',
    header: '영수증',
    cell: ({ row }) => (
      <div className="text-center">
        {row.getValue('hasReceipt') ? '✅' : '❌'}
      </div>
    ),
  },
];
