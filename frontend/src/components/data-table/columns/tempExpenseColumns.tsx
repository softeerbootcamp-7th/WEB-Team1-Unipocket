import type { ColumnDef } from '@tanstack/react-table';

import { CategoryChip } from '@/components/common/Chip';
import CurrencyBadge from '@/components/currency/CurrencyBadge';
import TempPaymentMethodCell from '@/components/data-table/cells/TempPaymentMethodCell';
import { Checkbox } from '@/components/ui/checkbox';

import type { TempExpense } from '@/api/temporary-expenses/type';
import { formatCurrency } from '@/lib/country';

interface TempExpenseColumnOptions {
  showBaseCurrencyAmount?: boolean;
  showPaymentMethod?: boolean;
}

export const getTempExpenseColumns = ({
  // 기본적으로 기준 금액은 보여준다고 가정 (필요에 따라 기본값은 변경 가능)
  showBaseCurrencyAmount = true,
  showPaymentMethod = true,
}: TempExpenseColumnOptions = {}): ColumnDef<TempExpense>[] => {
  const columns: (ColumnDef<TempExpense> | false)[] = [
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
      size: 30,
      header: () => <>거래처</>,
      meta: { cellEditor: 'text' },
      cell: ({ row }) => (
        <div className="relative flex items-center">
          <span className="truncate">{row.original.merchantName || '-'}</span>
        </div>
      ),
    },
    {
      accessorKey: 'category',
      size: 15,
      meta: { cellEditor: 'category' },
      header: () => <>카테고리</>,
      cell: ({ row }) => <CategoryChip categoryId={row.original.category} />,
    },
    {
      accessorKey: 'localCountryCode',
      size: 15,
      header: () => <>현지 통화</>,
      cell: ({ row }) => {
        const code = row.original.localCountryCode;
        return code ? <CurrencyBadge currencyCode={code} /> : <>-</>;
      },
    },
    {
      accessorKey: 'localCurrencyAmount',
      size: 20,
      meta: { cellEditor: 'amount' },
      header: () => <>현지 금액</>,
      cell: ({ row }) => {
        const amount = row.original.localCurrencyAmount;
        const code = row.original.localCountryCode;
        if (amount === null || !code) return <>-</>;
        return <>{formatCurrency(amount, code)}</>;
      },
    },

    showBaseCurrencyAmount && {
      accessorKey: 'baseCurrencyAmount',
      size: 20,
      meta: { cellEditor: 'amount' },
      header: () => <>기준 금액</>,
      cell: ({ row }) => {
        const amount = row.original.baseCurrencyAmount;
        const code = row.original.baseCountryCode;
        return <>{formatCurrency(amount, code)}</>;
      },
    },

    showPaymentMethod && {
      id: 'paymentMethod',
      size: 20, // (20%)
      header: () => <>결제 수단</>,
      meta: { cellEditor: 'method' },
      cell: ({ row }) => (
        <TempPaymentMethodCell
          cardLastFourDigits={row.original.cardLastFourDigits}
        />
      ),
    },
  ];

  // false로 들어간 값을 필터링해서 순수한 ColumnDef 배열만 반환
  return columns.filter(Boolean) as ColumnDef<TempExpense>[];
};
