import type { ColumnDef } from '@tanstack/react-table';

import type { Expense } from '@/components/landing-page/dummy';
import { Checkbox } from '@/components/ui/checkbox';

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
    accessorKey: 'merchantName',
    header: () => <>거래처</>,
    cell: ({ row }) => <> {row.getValue('merchantName')}</>,
  },
  {
    accessorKey: 'categoryCode',
    header: () => <>카테고리</>,
    cell: ({ row }) => <> {row.getValue('categoryCode')}</>,
  },
  {
    accessorKey: 'localCurrency',
    header: () => <>현지 통화</>,
    cell: ({ row }) => <> {row.getValue('localCurrency')}</>,
  },
  {
    accessorKey: 'localAmount',
    header: () => <>현지 금액</>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('localAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <>{formatted}</>;
    },
  },
  {
    accessorKey: 'standardAmount',
    header: () => <>기준 금액</>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('standardAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <>{formatted}</>;
    },
  },
  {
    accessorKey: 'exchangeRate',
    header: () => <>환율</>,
    cell: ({ row }) => <> {row.getValue('exchangeRate')}</>,
  },
  {
    id: 'paymentMethod',
    header: () => <>결제 수단</>,
    cell: ({ row }) => {
      const payment = row.original.paymentMethod;
      return <>{payment.isCash ? '현금' : payment.card?.label || '-'}</>;
    },
  },
  {
    id: 'travel',
    header: () => <>여행</>,
    cell: ({ row }) => <> {row.original.travel.name}</>,
  },
];
