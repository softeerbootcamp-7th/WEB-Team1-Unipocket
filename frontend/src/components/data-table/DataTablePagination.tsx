import type { ReactNode } from 'react';

import { useDataTable } from '@/components/data-table/context';

import { Icons } from '@/assets';

interface PaginationButtonProps {
  onClick: () => void;
  disabled: boolean;
  ariaLabel: string;
  children: ReactNode;
}

const PaginationButton = ({
  onClick,
  disabled,
  ariaLabel,
  children,
}: PaginationButtonProps) => {
  return (
    <button
      className="cursor-pointer disabled:cursor-not-allowed disabled:opacity-30"
      onClick={onClick}
      disabled={disabled}
      aria-label={ariaLabel}
    >
      {children}
    </button>
  );
};

interface DataTablePaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export const DataTablePagination = ({
  page,
  totalPages,
  onPageChange,
}: DataTablePaginationProps) => {
  const canGoPrev = page > 0;
  const canGoNext = page < totalPages - 1;
  const { dispatch } = useDataTable();

  if (totalPages <= 1) return null;

  const handlePageChange = (newPage: number) => {
    dispatch({ type: 'SET_ROW_SELECTION', payload: {} });
    onPageChange(newPage);
  };

  return (
    <div className="bg-background-normal relative flex items-center justify-center pt-1 pb-4">
      <div className="from-background-normal to-background-normal/0 pointer-events-none absolute -top-8 left-0 h-8 w-full bg-linear-to-t" />

      <PaginationButton
        onClick={() => handlePageChange(page - 1)} // 핸들러 변경
        disabled={!canGoPrev}
        ariaLabel="이전 페이지"
      >
        <Icons.ChevronBack className="size-4" />
      </PaginationButton>

      <div className="body2-normal-medium text-label-alternative px-4">
        {page + 1} / {Math.max(totalPages, 1)}
      </div>

      <PaginationButton
        onClick={() => handlePageChange(page + 1)} // 핸들러 변경
        disabled={!canGoNext}
        ariaLabel="다음 페이지"
      >
        <Icons.ChevronForward className="size-4" />
      </PaginationButton>
    </div>
  );
};
