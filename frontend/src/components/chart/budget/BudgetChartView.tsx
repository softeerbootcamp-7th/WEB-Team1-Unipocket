import BudgetAmountSummary from '@/components/chart/budget/BudgetAmountSummary';
import SemiCircleChart from '@/components/chart/charts/SemiCircleChart';
import { BUDGET_USAGE_RANGES } from '@/components/chart/chartType';

import type { CountryCode } from '@/data/country/countryCode';

interface BudgetChartViewProps {
  totalBudget: number;
  usedBudget: number;
  localUsedBudget: number;
  baseCode: CountryCode;
  localCode: CountryCode;
}

const BudgetChartView = ({
  totalBudget,
  usedBudget,
  localUsedBudget,
  baseCode,
  localCode,
}: BudgetChartViewProps) => {
  const rawPercentage = (usedBudget / totalBudget) * 100;
  const displayPercentage = Math.round(rawPercentage * 10) / 10;

  const currentStatus =
    BUDGET_USAGE_RANGES.find((range) => rawPercentage <= range.max) ||
    BUDGET_USAGE_RANGES[BUDGET_USAGE_RANGES.length - 1];

  return (
    <div className="flex w-full flex-col gap-3">
      {/* text section */}
      <BudgetAmountSummary
        totalBudget={totalBudget}
        usedBudget={usedBudget}
        localUsedBudget={localUsedBudget}
        baseCode={baseCode}
        localCode={localCode}
      />

      {/* chart section */}
      <div className="flex items-center justify-center">
        <SemiCircleChart value={displayPercentage} color={currentStatus.color}>
          {/* budget usage */}
          <div className="text-label-neutral flex flex-col items-center justify-end gap-1.5 pb-1">
            <span className="figure-heading1-semibold">
              {displayPercentage}%
            </span>
            <span className="figure-body2-14-semibold">
              {currentStatus.label}
            </span>
          </div>
        </SemiCircleChart>
      </div>
    </div>
  );
};

export default BudgetChartView;
