import clsx from 'clsx';

import { TOTAL_ANIMATION_DURATION } from '@/components/common/chart/chartType';

import { type CountryCode } from '@/data/countryCode';
import { formatCurrencyAmount } from '@/lib/country';

interface ReportBarProps {
  value: number;
  variant: 'me' | 'other';
  countryCode: CountryCode;
}

const ReportBar = ({ value, variant, countryCode }: ReportBarProps) => {
  return (
    <div className="flex items-center gap-2">
      <div
        className={clsx(
          'animate-expand-width h-3 w-57.75 origin-left rounded-r-xs',
          variant === 'me' ? 'bg-primary-normal' : 'bg-cool-neutral-95',
        )}
        style={{ animationDuration: `${TOTAL_ANIMATION_DURATION}s` }}
      />
      <span className="caption1-medium text-label-neutral">
        {formatCurrencyAmount(value, countryCode)}
      </span>
    </div>
  );
};

export default ReportBar;
