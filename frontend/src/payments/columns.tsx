import type { ColumnDef } from '@tanstack/react-table';

import { DataTableColumnCell } from '@/components/common/data-table/DataTableColumnCell';
import { Checkbox } from '@/components/ui/checkbox';

import { DataTableColumnHeader } from '../components/common/data-table/DataTableColumnHeader';
import type { Expense } from './dummy';

export const columns: ColumnDef<Expense>[] = [
  {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={
          table.getIsAllPageRowsSelected()
          // ||
          // (table.getIsSomePageRowsSelected() && 'indeterminate')
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
    accessorKey: 'storeName',
    header: () => <DataTableColumnHeader>거래처</DataTableColumnHeader>,
    cell: ({ row }) => (
      <DataTableColumnCell>{row.getValue('storeName')}</DataTableColumnCell>
    ),
  },
  {
    accessorKey: 'category.name', // 중첩 객체 접근
    header: () => <DataTableColumnHeader>카테고리</DataTableColumnHeader>,
    // cell: ({ row }) => (
    //   <DataTableColumnCell>{row.getValue('category')}</DataTableColumnCell>
    // ),
  },
  {
    id: 'amount',
    header: () => <DataTableColumnHeader>현지 금액</DataTableColumnHeader>,
    cell: ({ row }) => {
      const amount = row.original.amount;
      const currency = row.original.currency;
      return (
        <DataTableColumnCell>
          {amount.toLocaleString()} {currency}
        </DataTableColumnCell>
      );
    },
  },
  {
    accessorKey: 'krwAmount',
    header: () => <DataTableColumnHeader>원화 금액</DataTableColumnHeader>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('krwAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <DataTableColumnCell>{formatted}</DataTableColumnCell>;
    },
  },
];
