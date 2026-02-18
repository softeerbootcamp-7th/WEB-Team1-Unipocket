import type { ColumnDef } from '@tanstack/react-table';

import Chip from '@/components/common/Chip';
import SidePanelButton from '@/components/side-panel/SidePanelButton';
import { Checkbox } from '@/components/ui/checkbox';

import {
  CATEGORIES,
  type CategoryType,
  getCategoryName,
} from '@/types/category';

import type { ExpenseResponse } from '@/api/expenses/type';

export const columns: ColumnDef<ExpenseResponse>[] = [
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
    header: () => <>카테고리</>,
    cell: ({ row }) => {
      const categoryId = row.getValue('category') as CategoryType;
      const style = CATEGORIES[categoryId];
      return (
        <Chip
          label={getCategoryName(categoryId)}
          bg={style?.bg}
          text={style?.text}
        />
      );
    },
  },
  {
    accessorKey: 'localCurrencyCode',
    header: () => <>현지 통화</>,
    cell: ({ row }) => <> {row.getValue('localCurrencyCode')}</>,
  },
  {
    accessorKey: 'localCurrencyAmount',
    header: () => <>현지 금액</>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('localCurrencyAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <>{formatted}</>;
    },
  },
  {
    accessorKey: 'baseCurrencyAmount',
    header: () => <>기준 금액</>,
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('baseCurrencyAmount'));
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
    cell: ({ row }) => <> {row.original.travelId}</>,
  },
];
