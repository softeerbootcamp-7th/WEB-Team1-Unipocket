import clsx from 'clsx';

import { TOTAL_ANIMATION_DURATION } from '@/components/chart/chartType';
import CurrencyAmountDisplay from '@/components/currency/CurrencyAmountDisplay';
import CurrencyBadge from '@/components/currency/CurrencyBadge';

import type { CountryCode } from '@/data/countryCode';

interface ComparisonCardProps {
  variant: 'me' | 'average';
  barWidth: string;
  label: string;
  amount: number;
  countryCode: CountryCode;
  isLocal: boolean;
}

const VARIANT_STYLES = {
  me: {
    barColor: 'bg-primary-normal',
    textColor: 'text-primary-normal',
    amountTextColor: 'text-primary-normal',
  },
  average: {
    barColor: 'bg-cool-neutral-95',
    textColor: 'text-cool-neutral-70',
    amountTextColor: 'text-cool-neutral-70',
  },
} as const;

const ComparisonCard = ({
  variant,
  barWidth,
  label,
  amount,
  countryCode,
  isLocal,
}: ComparisonCardProps) => {
  const styles = VARIANT_STYLES[variant];

  return (
    <div className="flex h-8.5 items-center gap-3.5">
      <div
        className={clsx(
          'animate-expand-width h-8 origin-left items-center rounded-xs',
          barWidth,
          styles.barColor,
        )}
        style={{ animationDuration: `${TOTAL_ANIMATION_DURATION}s` }}
      />
      <div className="flex h-full flex-col justify-between">
        <span className={clsx('caption2-medium', styles.textColor)}>
          {label}
        </span>
        <div className="flex items-center gap-1.75">
          {isLocal && (
            <CurrencyBadge
              countryCode={countryCode}
              className={styles.amountTextColor}
            />
          )}
          <CurrencyAmountDisplay
            countryCode={countryCode}
            amount={amount}
            size="sm"
            className={styles.amountTextColor}
          />
        </div>
      </div>
    </div>
  );
};

export default ComparisonCard;
