import VerticalBarChart from '@/components/common/chart/charts/VerticalBarChart';
import { EXPENSE_SUSPENSE_COLOR } from '@/components/common/chart/chartType';
import { mockDataForCurrency } from '@/components/common/chart/expense/mock';

const ExpenseLegendItemSkeleton = () => {
  return (
    <div className="flex w-40 animate-pulse items-center justify-between">
      <div className="caption2-medium text-label-neutral flex items-center gap-1.75">
        <div
          className="size-3"
          style={{ backgroundColor: EXPENSE_SUSPENSE_COLOR[0] }}
        />
        <div
          className="rounded-modal-4 h-3 w-8"
          style={{ backgroundColor: EXPENSE_SUSPENSE_COLOR[0] }}
        />
        <div
          className="rounded-modal-4 h-3 w-10"
          style={{ backgroundColor: EXPENSE_SUSPENSE_COLOR[0] }}
        />
      </div>
      <div
        className="rounded-modal-4 h-3 w-6"
        style={{ backgroundColor: EXPENSE_SUSPENSE_COLOR[1] }}
      />
    </div>
  );
};

const ExpenseChartSkeleton = () => {
  const EXPENSE_LEGEND_COUNT = 5;
  return (
    <>
      <VerticalBarChart
        data={mockDataForCurrency}
        gap={2}
        colors={[EXPENSE_SUSPENSE_COLOR[0]]}
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
