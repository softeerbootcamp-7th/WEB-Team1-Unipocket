import type { CountryCode } from '@/data/countryCode';
import { formatCurrencyAmount, getCountryInfo } from '@/lib/country';
import { cn } from '@/lib/utils';

const SIZE_VARIANTS = {
  sm: {
    containerGap: 'gap-0.5',
    symbol: 'figure-caption1-medium text-label-alternative',
    integer: 'figure-body2-14-semibold',
    decimal: 'figure-caption1-medium -mt-0.5',
  },
  md: {
    containerGap: 'gap-0.5',
    symbol: 'figure-label2-medium text-label-alternative',
    integer: 'figure-body2-15-medium',
    decimal: 'figure-caption2-medium -mt-0.5',
  },
  lg: {
    containerGap: 'gap-1',
    symbol: 'title3-semibold',
    integer: 'title3-semibold',
    decimal: 'text-md font-medium',
  },
};

interface CurrencyAmountDisplayProps {
  countryCode: CountryCode;
  amount: number;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const CurrencyAmountDisplay = ({
  countryCode,
  amount,
  size = 'md',
  className,
}: CurrencyAmountDisplayProps) => {
  const countryInfo = getCountryInfo(countryCode);

  if (!countryInfo) return null;

  // KRW(원), JPY(엔), VND(동) 등은 소수점을 쓰지 않는 것이 관례
  const isZeroDecimal = new Set(['KRW', 'JPY', 'VND']).has(
    countryInfo.currencyName,
  );
  const fractionDigits = isZeroDecimal ? 0 : 2;

  const formattedString = formatCurrencyAmount(
    amount,
    countryCode,
    fractionDigits,
  );

  const [integerPart, decimalPart] = formattedString.split('.');

  const styles = SIZE_VARIANTS[size];

  return (
    <div
      className={cn(
        'text-label-normal flex items-center',
        styles.containerGap,
        className,
      )}
    >
      {/* 통화 기호 */}
      <span className={cn(styles.symbol, className)}>
        {countryInfo.currencySign}
      </span>

      {/* 숫자 영역 (정수 + 소수) */}
      <div className="flex items-center">
        <span className={cn(styles.integer, className)}>{integerPart}</span>

        {decimalPart && (
          <span
            className={cn(
              'text-label-alternative',
              className && 'opacity-80',
              styles.decimal,
              className,
            )}
          >
            .{decimalPart}
          </span>
        )}
      </div>
    </div>
  );
};

export default CurrencyAmountDisplay;
