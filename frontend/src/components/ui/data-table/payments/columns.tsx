import type { ColumnDef } from '@tanstack/react-table';

import { Checkbox } from '@/components/ui/checkbox';

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
    header: () => <>거래처</>,
    cell: ({ row }) => <> {row.getValue('storeName')}</>,
  },
  {
    accessorKey: 'category.name', // 중첩 객체 접근
    header: () => <>카테고리</>,
    // cell: ({ row }) => (
    //   <DataTableCell>{row.getValue('category')}</DataTableCell>
    // ),
  },
  {
    id: 'amount',
    header: () => <>현지 금액</>,
    cell: ({ row }) => {
      const amount = row.original.amount;
      const currency = row.original.currency;
      return (
        <>
          {amount.toLocaleString()} {currency}
        </>
      );
    },
  },
  {
    accessorKey: 'krwAmount',
    header: () => <>현지 금액</>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('krwAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <>{formatted}</>;
    },
  },
];
