import { Icons } from '@/assets';

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

  return (
    <div className="bg-background-normal relative flex items-center justify-center pt-1 pb-4">
      {/* 상단 페이드(블러) 효과 */}
      <div className="from-background-normal to-background-normal/0 pointer-events-none absolute -top-8 left-0 h-8 w-full bg-linear-to-t" />

      <button
        className="cursor-pointer disabled:cursor-not-allowed disabled:opacity-30"
        onClick={() => canGoPrev && onPageChange(page - 1)}
        disabled={!canGoPrev}
        aria-label="이전 페이지"
      >
        <Icons.ChevronBack className="size-4" />
      </button>

      <div className="body2-normal-medium text-label-alternative px-4">
        {page + 1} / {Math.max(totalPages, 1)}
      </div>

      <button
        className="cursor-pointer disabled:cursor-not-allowed disabled:opacity-30"
        onClick={() => canGoNext && onPageChange(page + 1)}
        disabled={!canGoNext}
        aria-label="다음 페이지"
      >
        <Icons.ChevronForward className="size-4" />
      </button>
    </div>
  );
};
