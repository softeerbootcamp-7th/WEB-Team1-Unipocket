import type { ColumnDef } from '@tanstack/react-table';

import { CategoryChip } from '@/components/common/Chip';
import CurrencyBadge from '@/components/currency/CurrencyBadge';
import PaymentMethodDisplay from '@/components/expense/PaymentMethodDisplay';
import SidePanelButton from '@/components/side-panel/SidePanelButton';
import { Checkbox } from '@/components/ui/checkbox';

import { type CategoryId } from '@/types/category';

import type { Expense } from '@/api/expenses/type';
import type { CurrencyCode } from '@/data/country/currencyCode';
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
        <span>{row.getValue('merchantName')}</span>
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
      const categoryId = row.getValue('category') as CategoryId;
      return <CategoryChip categoryId={categoryId} />;
    },
  },
  {
    accessorKey: 'localCurrencyCode',
    header: () => <>현지 통화</>,
    cell: ({ row }) => (
      <>
        <CurrencyBadge
          currencyCode={row.getValue('localCurrencyCode') as CurrencyCode}
        />
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
      const rate = parseFloat(row.getValue('exchangeRate'));

      const formatted = new Intl.NumberFormat('ko-KR', {
        maximumFractionDigits: 0,
      }).format(rate);
      return <> {formatted}</>;
    },
  },
  {
    id: 'paymentMethod',
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
    id: 'travel',
    meta: {
      cellEditor: 'travel',
    },
    header: () => <>여행</>,
    cell: ({ row }) => {
      const travelName = row.original.travel?.name || '-';
      return (
        <div
          className="w-10 truncate text-left lg:w-20"
          title={travelName !== '-' ? travelName : undefined}
        >
          {travelName}
        </div>
      );
    },
  },
];
