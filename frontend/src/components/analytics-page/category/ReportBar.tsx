import { useEffect, useState } from 'react';
import clsx from 'clsx';

import { TOTAL_ANIMATION_DURATION } from '@/components/common/chart/chartType';
import CurrencyAmountDisplay from '@/components/common/currency/CurrencyAmountDisplay';
import CurrencyBadge from '@/components/common/currency/CurrencyBadge';

import { type CountryCode } from '@/data/countryCode';

interface ReportBarProps {
  value: number;
  variant: 'me' | 'other';
  countryCode: CountryCode;
  maxValue: number;
}

const VARIANT_STYLES = {
  me: {
    bgColor: 'bg-primary-normal',
    textStyle: 'text-primary-normal body2-normal-bold',
    size: 'md',
  },
  other: {
    bgColor: 'bg-cool-neutral-95',
    textStyle: 'text-cool-neutral-80 label2-medium',
    size: 'sm',
  },
} as const;

// @TODO: 페이지에서 받아오도록 수정 필요 (필터 사용)
const currency = 'LOCAL';

const ReportBar = ({
  value,
  variant,
  countryCode,
  maxValue,
}: ReportBarProps) => {
  const styles = VARIANT_STYLES[variant];
  const percentage = (value / maxValue) * 100;
  const [showAmount, setShowAmount] = useState(false);

  const isLocal = currency === 'LOCAL';

  useEffect(() => {
    const timer = setTimeout(() => {
      setShowAmount(true);
    }, TOTAL_ANIMATION_DURATION * 1000);

    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="flex flex-1 items-center gap-2">
      <div
        className={clsx(
          'animate-expand-width h-3 origin-left rounded-r-xs',
          styles.bgColor,
        )}
        style={{
          width: `${percentage}%`,
          animationDuration: `${TOTAL_ANIMATION_DURATION}s`,
        }}
      />
      {showAmount && (
        <div className="flex gap-1">
          {isLocal && (
            <CurrencyBadge
              countryCode={countryCode}
              className={styles.textStyle}
            />
          )}

          <CurrencyAmountDisplay
            amount={value}
            countryCode={countryCode}
            size={styles.size}
            className={styles.textStyle}
          />
        </div>
      )}
    </div>
  );
};

export default ReportBar;
