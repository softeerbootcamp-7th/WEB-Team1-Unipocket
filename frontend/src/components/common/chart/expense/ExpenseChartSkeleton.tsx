import VerticalBarChart from '@/components/common/chart/charts/VerticalBarChart';
import { mockDataForCurrency } from '@/components/common/chart/expense/mock';

const ExpenseLegendItemSkeleton = () => {
  return (
    <div className="flex w-40 animate-pulse items-center justify-between">
      <div className="caption2-medium text-label-neutral flex items-center gap-1.75">
        <div className="bg-fill-strong size-3" />
        <div className="rounded-modal-4 bg-fill-strong h-3 w-8" />
        <div className="rounded-modal-4 bg-fill-strong h-3 w-10" />
      </div>
      <div className="rounded-modal-4 bg-fill-normal h-3 w-6" />
    </div>
  );
};

const ExpenseChartSkeleton = () => {
  const EXPENSE_LEGEND_COUNT = 4;
  return (
    <>
      <VerticalBarChart
        data={mockDataForCurrency}
        gap={2}
        colors={['var(--color-fill-strong)']}
        animate={false}
      />
      <div className="flex flex-col items-start justify-center gap-2.5">
        {Array.from({ length: EXPENSE_LEGEND_COUNT }).map((_, idx) => (
          <ExpenseLegendItemSkeleton key={idx} />
        ))}
      </div>
    </>
  );
};

export default ExpenseChartSkeleton;
