import type { CountryCode } from '@/data/country/countryCode';
import { formatAmountByCountry, getCountryInfo } from '@/lib/country';
import { cn } from '@/lib/utils';

const SIZE_VARIANTS = {
  xs: {
    containerGap: 'gap-0.5',
    symbol: 'caption1-medium',
    integer: 'caption1-medium',
    decimal: 'caption1-medium',
  },
  sm: {
    containerGap: 'gap-0.5',
    symbol: 'figure-caption1-medium',
    integer: 'figure-body2-14-semibold',
    decimal: 'figure-caption1-medium',
  },
  md: {
    containerGap: 'gap-0.5',
    symbol: 'figure-label2-medium',
    integer: 'figure-body2-15-medium',
    decimal: 'figure-caption2-medium',
  },
  lg: {
    containerGap: 'gap-1',
    symbol: 'title3-semibold',
    integer: 'title3-semibold',
    decimal: 'text-md font-medium',
  },
  folder_sm: {
    containerGap: 'gap-1',
    symbol: 'figure-label2-small-medium',
    integer: 'figure-label2-small-medium',
    decimal: '',
  },
  folder_lg: {
    containerGap: 'gap-1',
    symbol: 'figure-body2-18-semibold',
    integer: 'figure-body2-18-semibold',
    decimal: '',
  },
};

const COLOR_VARIANTS = {
  default: {
    symbol: 'text-label-alternative',
    integer: 'text-label-normal',
    decimal: 'text-label-alternative',
  },
  inverse: {
    symbol: 'text-background-normal',
    integer: 'text-background-normal',
    decimal: 'text-background-normal',
  },
  muted: {
    symbol: 'text-cool-neutral-97',
    integer: 'text-cool-neutral-97',
    decimal: 'text-cool-neutral-97',
  },
  folder_sm: {
    containerGap: 'gap-1',
    symbol: 'figure-label2-small-medium',
    integer: 'figure-label2-small-medium',
    decimal: '',
  },
  folder_lg: {
    containerGap: 'gap-1',
    symbol: 'figure-body2-18-semibold',
    integer: 'figure-body2-18-semibold',
    decimal: '',
  },
};

interface CurrencyAmountDisplayProps {
  countryCode: CountryCode;
  amount: number;
  className?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'folder_sm' | 'folder_lg';
  variant?: 'default' | 'inverse' | 'muted';
}

const CurrencyAmountDisplay = ({
  countryCode,
  amount,
  size = 'md',
  variant = 'default',
  className,
}: CurrencyAmountDisplayProps) => {
  const countryInfo = getCountryInfo(countryCode);

  if (!countryInfo) return null;

  // KRW(원), JPY(엔), VND(동) 등은 소수점을 쓰지 않는 것이 관례
  const isZeroDecimal = new Set(['KRW', 'JPY', 'VND']).has(
    countryInfo.currencyName,
  );
  const fractionDigits = isZeroDecimal ? 0 : 2;

  const formattedString = formatAmountByCountry(
    amount,
    countryCode,
    fractionDigits,
  );

  const sizeStyles = SIZE_VARIANTS[size];
  const colorStyles = COLOR_VARIANTS[variant];

  return (
    <div
      className={cn('flex items-center', sizeStyles.containerGap, className)}
    >
      {/* 통화 기호 */}
      <span
        className={cn(
          sizeStyles.symbol,
          colorStyles.symbol,
          size === 'xs' && 'text-label-alternative',
          className,
        )}
      >
        {countryInfo.currencySign}
      </span>

      {/* 숫자 영역 */}
      <span className={cn(sizeStyles.integer, colorStyles.integer, className)}>
        {formattedString}
      </span>
    </div>
  );
};

export default CurrencyAmountDisplay;
