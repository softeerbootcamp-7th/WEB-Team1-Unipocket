import { countryCode } from '@/data/countryCode';

import SemiCircleChart from '../charts/SemiCircleChart';
import { BUDGET_USAGE_RANGES } from '../chartType';
import BudgetAmountSummary from './BudgetAmountSummary';

interface BudgetChartViewProps {
  totalBudget: number;
  usedBudget: number;
}

// 임시용 상수. 해당 값 전역화 시 제거 필요
const baseCode = countryCode[0];
const localCode = countryCode[12];

const BudgetChartView = ({ totalBudget, usedBudget }: BudgetChartViewProps) => {
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
