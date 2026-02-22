import clsx from 'clsx';

import type { CountryCode } from '@/data/country/countryCode';
import type { CurrencyCode } from '@/data/country/currencyCode';
import { getCountryInfo, getCountryInfoByCurrency } from '@/lib/country'; // 헬퍼 함수 추가 필요
import { cn } from '@/lib/utils';

// 둘 중 하나만 필수로 받도록 타입을 지정할 수도 있지만, 간결함을 위해 둘 다 optional로 처리
interface CurrencyBadgeProps {
  countryCode?: CountryCode;
  currencyCode?: CurrencyCode;
  size?: 'sm' | 'md';
  className?: string;
}

const CurrencyBadge = ({
  countryCode,
  currencyCode,
  size = 'md',
  className,
}: CurrencyBadgeProps) => {
  const countryInfo = countryCode
    ? getCountryInfo(countryCode)
    : currencyCode
      ? getCountryInfoByCurrency(currencyCode)
      : null;

  if (!countryInfo) return null;

  const isMd = size === 'md';

  return (
    <div className="flex items-center gap-1">
      <div className={clsx(isMd ? 'h-3.5' : 'h-2.2')}>
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
