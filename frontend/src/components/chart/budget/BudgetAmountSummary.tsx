import CurrencyAmountDisplay from '@/components/currency/CurrencyAmountDisplay';
import CurrencyBadge from '@/components/currency/CurrencyBadge';

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
        <CurrencyAmountDisplay
          countryCode={baseCode}
          amount={totalBudget}
          size="xs"
        />
        <span className="caption1-medium text-label-alternative">중</span>
      </div>
      <div className="flex w-full flex-col items-start">
        <div className="flex items-center gap-1.75">
          <CurrencyAmountDisplay
            countryCode={baseCode}
            amount={usedBudget}
            size="lg"
          />
          <span className="body1-normal-bold text-label-neutral">사용</span>
        </div>
        <div className="flex items-center gap-1.5 py-0.75">
          <CurrencyBadge countryCode={localCode} />
          <CurrencyAmountDisplay
            countryCode={localCode}
            amount={localUsedBudget}
            className="text-label-neutral"
          />
        </div>
      </div>
    </div>
  );
};

export default BudgetAmountSummary;
