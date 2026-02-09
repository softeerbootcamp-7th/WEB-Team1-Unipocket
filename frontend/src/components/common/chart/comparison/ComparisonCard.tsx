import CurrencyAmountDisplay from '@/components/common/currency/CurrencyAmountDisplay';
import CurrencyBadge from '@/components/common/currency/CurrencyBadge';

import type { CountryCode } from '@/data/countryCode';

interface ComparisonCardProps {
  barWidth: string;
  barColor: 'bg-cool-neutral-95' | 'bg-primary-normal';
  label: string;
  amount: number;
  countryCode: CountryCode;
  isLocal: boolean;
  textColor: 'text-cool-neutral-80' | 'text-primary-normal';
  amountTextColor: 'text-cool-neutral-70' | 'text-primary-normal';
}

const ComparisonCard = ({
  barWidth,
  barColor,
  label,
  amount,
  countryCode,
  isLocal,
  textColor,
  amountTextColor,
}: ComparisonCardProps) => {
  return (
    <div className="flex h-8.5 gap-3.5">
      <div
        className={`h-8 ${barWidth} items-center rounded-xs animate-expand-width origin-left ${barColor}`}
      />
      <div className="flex flex-col gap-1.5">
        <span className={`caption2-medium ${textColor}`}>{label}</span>
        <div className="flex items-center gap-1.75">
          {isLocal && (
            <CurrencyBadge countryCode={countryCode} className={amountTextColor} />
          )}
          <CurrencyAmountDisplay
            countryCode={countryCode}
            amount={amount}
            size="sm"
            className={amountTextColor}
          />
        </div>
      </div>
    </div>
  );
};

export default ComparisonCard;
