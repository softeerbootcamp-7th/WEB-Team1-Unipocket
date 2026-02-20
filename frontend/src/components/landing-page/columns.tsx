import type { ColumnDef } from '@tanstack/react-table';

import type { Expense } from '@/api/expenses/type';

export const columns: ColumnDef<Expense>[] = [
  {
    accessorKey: 'merchantName',
    header: () => <>거래처</>,
    meta: {
      cellEditor: 'text',
    },
    cell: ({ row }) => <> {row.getValue('merchantName')}</>,
  },
  {
    accessorKey: 'categoryCode',
    meta: {
      cellEditor: 'category',
    },
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
    meta: {
      cellEditor: 'amount',
    },
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
    meta: {
      cellEditor: 'amount',
    },
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
    meta: {
      cellEditor: 'method',
    },
    cell: ({ row }) => {
      const payment = row.original.paymentMethod;
      return <>{payment.isCash ? '현금' : payment.card?.label || '-'}</>;
    },
  },
  {
    id: 'travel',
    header: () => <>여행</>,
    meta: {
      cellEditor: 'text',
    },
    cell: ({ row }) => <> {row.original.travel?.name || '-'}</>,
  },
];
