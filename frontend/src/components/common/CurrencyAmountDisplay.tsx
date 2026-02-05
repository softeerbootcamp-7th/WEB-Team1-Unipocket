import clsx from 'clsx';

import type { CountryCode } from '@/data/countryCode';
import { formatCurrencyAmount, getCountryInfo } from '@/lib/country';

const SIZE_VARIANTS = {
  sm: {
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
  size?: 'sm' | 'lg';
}

const CurrencyAmountDisplay = ({
  countryCode,
  amount,
  size = 'sm',
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
      className={clsx(
        'text-label-normal flex items-center',
        styles.containerGap,
      )}
    >
      {/* 통화 기호 */}
      <span className={styles.symbol}>{countryInfo.currencySign}</span>

      {/* 숫자 영역 (정수 + 소수) */}
      <div className="flex items-start">
        <span className={styles.integer}>{integerPart}</span>

        {decimalPart && (
          <span className={clsx('text-label-alternative', styles.decimal)}>
            .{decimalPart}
          </span>
        )}
      </div>
    </div>
  );
};

export default CurrencyAmountDisplay;
