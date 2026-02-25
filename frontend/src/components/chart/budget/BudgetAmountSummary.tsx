import CurrencyBadge from '@/components/currency/CurrencyBadge';
import ScaledAmountDisplay from '@/components/currency/ScaledAmountDisplay';

import type { CountryCode } from '@/data/country/countryCode';

interface BudgetAmountSummaryProps {
  totalBudget: number;
  usedBudget: number;
  localUsedBudget: number;
  baseCode: CountryCode;
  localCode: CountryCode;
}

const BudgetAmountSummary = ({
  totalBudget,
  usedBudget,
  localUsedBudget,
  baseCode,
  localCode,
}: BudgetAmountSummaryProps) => {
  return (
    <div className="flex w-full flex-col items-start gap-1.5">
      <div className="flex items-center gap-0.5">
        <ScaledAmountDisplay
          countryCode={baseCode}
          amount={totalBudget}
          maxWidth={180}
          size="xs"
        />
        <span className="caption1-medium text-label-alternative">중</span>
      </div>
      <div className="flex w-full flex-col items-start">
        <div className="flex items-center gap-1.75">
          <ScaledAmountDisplay
            countryCode={baseCode}
            amount={usedBudget}
            maxWidth={180}
            size="lg"
          />
          <span className="body1-normal-bold text-label-neutral shrink-0">
            사용
          </span>
        </div>
        <div className="flex items-center gap-1.5 py-0.75">
          <CurrencyBadge countryCode={localCode} />
          <ScaledAmountDisplay
            countryCode={localCode}
            amount={localUsedBudget}
            maxWidth={180}
            className="text-label-neutral"
          />
        </div>
      </div>
    </div>
  );
};

export default BudgetAmountSummary;
