import clsx from 'clsx';

import type { CountryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';

interface CurrencyBadgeProps {
  countryCode: CountryCode;
  color?: string;
  size?: 'sm' | 'md';
}

const CurrencyBadge = ({
  countryCode,
  color,
  size = 'md',
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
        className={clsx(
          isMd ? 'figure-label2-medium' : 'figure-label2-small-medium',
          !color && 'text-label-neutral',
        )}
        style={color ? { color } : undefined}
      >
        {countryInfo.currencyName}
      </span>
    </div>
  );
};

export default CurrencyBadge;
