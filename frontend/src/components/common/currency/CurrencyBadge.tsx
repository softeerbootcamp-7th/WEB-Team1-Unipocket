import clsx from 'clsx';

import type { CountryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';
import { cn } from '@/lib/utils';

interface CurrencyBadgeProps {
  countryCode: CountryCode;
  size?: 'sm' | 'md';
  className?: string;
}

const CurrencyBadge = ({
  countryCode,
  size = 'md',
  className,
}: CurrencyBadgeProps) => {
  const countryInfo = getCountryInfo(countryCode);

  if (!countryInfo) return null;

  const isMd = size === 'md';

  return (
    <div className="flex items-center gap-1">
      <div className={clsx(size === 'md' ? 'h-3.5' : 'h-2.2')}>
        <img
          className={clsx(
            isMd ? 'rounded-modal-4 h-3 w-3' : 'rounded-modal-2 h-2 w-3',
          )}
          src={countryInfo.imageUrl}
          alt={`${countryInfo.currencyName} flag`}
        />
      </div>
      <span
        className={cn(
          'text-label-neutral',
          isMd ? 'figure-label2-medium' : 'figure-label2-small-medium',
          className,
        )}
      >
        {countryInfo.currencyName}
      </span>
    </div>
  );
};

export default CurrencyBadge;
