'use client';

import { Checkbox } from '@/components/ui/checkbox';
import type { ColumnDef } from '@tanstack/react-table';
import { DataTableColumnHeader } from '../components/data-table/DataTableColumnHeader';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

// This type is used to define the shape of our data.
// You can use a Zod schema here if you want.
export type Payment = {
  id: string;
  date: string;
  merchant: string;
  category: string;
  localCurrency: string;
  localAmount: number;
  baseAmount: number;
  exchangeRate: number;
  paymentMethod: string;
  trip?: string;
};

export const columns: ColumnDef<Payment>[] = [
  {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={
          table.getIsAllPageRowsSelected() ||
          (table.getIsSomePageRowsSelected() && 'indeterminate')
        }
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
    id: 'actions',
    cell: ({ row }) => {
      const payment = row.original;

      return (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="h-8 w-8 bg-violet-200 p-0">
              <span className="sr-only">Open menu</span>
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuLabel>Actions</DropdownMenuLabel>
            <DropdownMenuItem
              onClick={() => navigator.clipboard.writeText(payment.id)}
            >
              Copy payment ID
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem>View customer</DropdownMenuItem>
            <DropdownMenuItem>View payment details</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      );
    },
  },
  {
    accessorKey: 'date',
    header: ({ column }) => {
      return (
        <button
          onClick={() => column.toggleSorting(column.getIsSorted() === 'asc')}
        >
          <div className="ml-2 h-4 w-4 bg-amber-200">날짜</div>
        </button>
      );
    },
  },
  {
    accessorKey: 'merchant',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="거래처" />
    ),
  },
  {
    accessorKey: 'category',
    header: '카테고리',
  },
  {
    accessorKey: 'localCurrency',
    header: '현지 금액',
  },
  {
    accessorKey: 'localAmount',
    header: '기준 금액(W)',
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue('localAmount'));
      const formatted = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
      }).format(amount);
      return <div className="text-right">{formatted}</div>;
    },
  },
  {
    accessorKey: 'baseAmount',
    header: '환율(W)',
    cell: ({ row }) => {
      // const amount = row.getValue('baseAmount');
      return <div className="text-right">amt</div>;
    },
  },
  {
    accessorKey: 'exchangeRate',
    header: '결제 수단',
  },
  {
    accessorKey: 'paymentMethod',
    header: '여행',
  },
  {
    accessorKey: 'trip',
    header: '여행',
  },
];
