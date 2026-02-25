import type { ColumnDef } from '@tanstack/react-table';

import { CategoryChip } from '@/components/common/Chip';
import CurrencyBadge from '@/components/currency/CurrencyBadge';
import PaymentMethodDisplay from '@/components/expense/PaymentMethodDisplay';
import { Checkbox } from '@/components/ui/checkbox';

import type { Expense } from '@/api/expenses/type';
import { formatCurrency } from '@/lib/country';

export const expenseColumns: ColumnDef<Expense>[] = [
  {
    id: 'select',
    size: 4,
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
    size: 16,
    header: () => <>거래처</>,
    meta: {
      cellEditor: 'text',
    },
    cell: ({ row }) => (
      <div className="relative flex items-center">
        <span className="truncate">{row.original.merchantName}</span>
      </div>
    ),
  },
  {
    accessorKey: 'category',
    size: 10,
    meta: {
      cellEditor: 'category',
    },
    header: () => <>카테고리</>,
    cell: ({ row }) => {
      const categoryId = row.original.category;
      return <CategoryChip categoryId={categoryId} />;
    },
  },
  {
    accessorKey: 'localCurrencyCode',
    size: 10,
    header: () => <>현지 통화</>,
    cell: ({ row }) => (
      <>
        <CurrencyBadge currencyCode={row.original.localCurrencyCode} />
      </>
    ),
  },
  {
    accessorKey: 'localCurrencyAmount',
    size: 10,
    meta: {
      cellEditor: 'amount',
    },
    header: () => <>현지 금액</>,
    cell: ({ row }) => {
      const amount = row.original.localCurrencyAmount;
      const code = row.original.localCurrencyCode;
      return <>{formatCurrency(amount, code)}</>;
    },
  },
  {
    accessorKey: 'baseCurrencyAmount',
    size: 10,
    meta: {
      cellEditor: 'amount',
    },
    header: () => <>기준 금액</>,
    cell: ({ row }) => {
      const amount = row.original.baseCurrencyAmount;
      const code = row.original.baseCurrencyCode;
      return <>{formatCurrency(amount, code)}</>;
    },
  },
  {
    accessorKey: 'exchangeRate',
    size: 10,
    header: () => <>환율</>,
    cell: ({ row }) => {
      const amount = row.original.exchangeRate;
      const code = row.original.baseCurrencyCode;
      return <>{formatCurrency(amount, code)}</>;
    },
  },
  {
    accessorKey: 'paymentMethod',
    size: 15,
    header: () => <>결제 수단</>,
    meta: {
      cellEditor: 'method',
    },
    cell: ({ row }) => {
      const payment = row.original.paymentMethod;
      return <PaymentMethodDisplay paymentMethod={payment} />;
    },
  },
  {
    accessorKey: 'travel',
    size: 15,
    meta: {
      cellEditor: 'travel',
    },
    header: () => <>여행</>,
    cell: ({ row }) => {
      const travelName = row.original.travel?.name || '-';
      return <div className="truncate">{travelName}</div>;
    },
  },
];
