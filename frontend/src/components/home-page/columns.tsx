import type { ColumnDef } from '@tanstack/react-table';

import { CategoryChip } from '@/components/common/Chip';
import CurrencyBadge from '@/components/currency/CurrencyBadge';
import PaymentMethodDisplay from '@/components/expense/PaymentMethodDisplay';
import SidePanelButton from '@/components/side-panel/SidePanelButton';
import { Checkbox } from '@/components/ui/checkbox';

import type { Expense } from '@/api/expenses/type';
import { formatCurrency } from '@/lib/country';

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
    meta: {
      cellEditor: 'text',
    },
    cell: ({ row, table }) => (
      <div className="relative flex items-center">
        <span>{row.original.merchantName}</span>
        <div className="absolute right-0 z-10">
          <SidePanelButton row={row} table={table} />
        </div>
      </div>
    ),
  },
  {
    accessorKey: 'category',
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
    header: () => <>현지 통화</>,
    cell: ({ row }) => (
      <>
        <CurrencyBadge currencyCode={row.original.localCurrencyCode} />
      </>
    ),
  },
  {
    accessorKey: 'localCurrencyAmount',
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
    header: () => <>환율</>,
    cell: ({ row }) => {
      const amount = row.original.exchangeRate;
      const code = row.original.baseCurrencyCode;
      return <>{formatCurrency(amount, code)}</>;
    },
  },
  {
    accessorKey: 'paymentMethod',
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
    meta: {
      cellEditor: 'text',
    },
    header: () => <>여행</>,
    cell: ({ row }) => <> {row.original.travel?.name || '-'}</>,
  },
];
