import type { ColumnDef } from '@tanstack/react-table';

import { DataTableCell } from '@/components/common/data-table/DataTableCell';
import { Checkbox } from '@/components/ui/checkbox';

import { DataTableHeader } from '../components/common/data-table/DataTableHeader';
import type { Expense } from './dummy';

export const columns: ColumnDef<Expense>[] = [
  {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={table.getIsAllPageRowsSelected()}
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
  {
    accessorKey: 'storeName',
    header: () => <DataTableHeader>거래처</DataTableHeader>,
    cell: ({ row }) => (
      <DataTableCell>{row.getValue('storeName')}</DataTableCell>
    ),
  },
  {
    accessorKey: 'category.name', // 중첩 객체 접근
    header: () => <DataTableHeader>카테고리</DataTableHeader>,
    // cell: ({ row }) => (
    //   <DataTableCell>{row.getValue('category')}</DataTableCell>
    // ),
  },
  {
    id: 'amount',
    header: () => <DataTableHeader>현지 금액</DataTableHeader>,
    cell: ({ row }) => {
      const amount = row.original.amount;
      const currency = row.original.currency;
      return (
        <DataTableCell>
          {amount.toLocaleString()} {currency}
        </DataTableCell>
      );
    },
  },
  {
    accessorKey: 'krwAmount',
    header: () => <DataTableHeader>원화 금액</DataTableHeader>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('krwAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <DataTableCell>{formatted}</DataTableCell>;
    },
  },
];
