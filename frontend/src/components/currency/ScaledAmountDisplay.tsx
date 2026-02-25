import { useAutoFitScale } from '@/hooks/useAutoFitScale';

import CurrencyAmountDisplay from '@/components/currency/CurrencyAmountDisplay';

import type { CountryCode } from '@/data/country/countryCode';

interface ScaledAmountDisplayProps {
  countryCode: CountryCode;
  amount: number;
  maxWidth: number;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'folder_sm' | 'folder_lg';
  variant?: 'default' | 'inverse' | 'muted';
  className?: string;
}

const ScaledAmountDisplay = ({
  countryCode,
  amount,
  maxWidth,
  ...props
}: ScaledAmountDisplayProps) => {
  const { ref, scale } = useAutoFitScale<HTMLDivElement>(maxWidth, [
    amount,
    countryCode,
  ]);

  return (
    <div
      ref={ref}
      style={{ transform: `scale(${scale})`, transformOrigin: 'left center' }}
    >
      <CurrencyAmountDisplay
        countryCode={countryCode}
        amount={amount}
        {...props}
      />
    </div>
  );
};

export default ScaledAmountDisplay;
