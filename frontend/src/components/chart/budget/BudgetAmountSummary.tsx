import { useAutoFitScale } from '@/hooks/useAutoFitScale';

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
  const { ref: totalRef, scale: totalScale } = useAutoFitScale<HTMLDivElement>(
    180,
    [totalBudget, baseCode],
  );
  const { ref: usedRef, scale: usedScale } = useAutoFitScale<HTMLDivElement>(
    180,
    [usedBudget, baseCode],
  );
  const { ref: localRef, scale: localScale } = useAutoFitScale<HTMLDivElement>(
    180,
    [localUsedBudget, localCode],
  );

  return (
    <div className="flex w-full flex-col items-start gap-1.5">
      <div className="flex items-center gap-0.5">
        <div
          ref={totalRef}
          style={{
            transform: `scale(${totalScale})`,
            transformOrigin: 'left center',
          }}
        >
          <CurrencyAmountDisplay
            countryCode={baseCode}
            amount={totalBudget}
            size="xs"
          />
        </div>
        <span className="caption1-medium text-label-alternative">중</span>
      </div>
      <div className="flex w-full flex-col items-start">
        <div className="flex items-center gap-1.75">
          <div
            ref={usedRef}
            style={{
              transform: `scale(${usedScale})`,
              transformOrigin: 'left center',
            }}
            className="max-w-45"
          >
            <CurrencyAmountDisplay
              countryCode={baseCode}
              amount={usedBudget}
              size="lg"
            />
          </div>
          <span className="body1-normal-bold text-label-neutral shrink-0">
            사용
          </span>
        </div>
        <div className="flex items-center gap-1.5 py-0.75">
          <CurrencyBadge countryCode={localCode} />
          <div
            ref={localRef}
            style={{
              transform: `scale(${localScale})`,
              transformOrigin: 'left center',
            }}
          >
            <CurrencyAmountDisplay
              countryCode={localCode}
              amount={localUsedBudget}
              className="text-label-neutral max-w-45"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default BudgetAmountSummary;
