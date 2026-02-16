import type { ColumnDef } from '@tanstack/react-table';

import type { TableUIAction } from '@/components/data-table/type';
import type { Expense } from '@/components/landing-page/dummy';
import { Checkbox } from '@/components/ui/checkbox';

import { cn } from '@/lib/utils';

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
    cell: ({ row, table }) => {
      const meta = table.options.meta as {
        dispatch: React.Dispatch<TableUIAction>;
      };

      const handleOpenSidePanel = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.stopPropagation();
        e.currentTarget.blur();
        meta?.dispatch({
          type: 'SET_ACTIVE_ROW',
          payload: {
            rowId: row.id,
            value: row.original,
          },
        });
      };

      return (
        <div className="flex items-center gap-2">
          <span>{row.getValue('merchantName')}</span>
          <button
            onClick={handleOpenSidePanel}
            className={cn(
              'rounded-modal-6 shadow-semantic-emphasize bg-background-normal px-1.25 py-1',
              'label2-medium text-label-neutral',
              'invisible cursor-pointer group-hover/row:visible focus:visible',
            )}
          >
            열기
          </button>
        </div>
      );
    },
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
