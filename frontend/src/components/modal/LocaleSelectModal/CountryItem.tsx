import clsx from 'clsx';

import Control from '@/components/common/Control';

import type { CountryCode } from '@/data/country/countryCode';

interface CountryItemProps {
  flagImg: string;
  country: string;
  currency: string;
  checked: boolean;
  value: CountryCode;
  onChange: (value: CountryCode) => void;
  isLast: boolean;
}

const CountryItem = ({
  flagImg,
  country,
  currency,
  checked,
  value,
  onChange,
  isLast,
}: CountryItemProps) => {
  return (
    <div
      className={clsx(
        'border-line-normal-normal flex w-118 cursor-pointer items-center justify-center gap-6 border-b py-5 pr-2.5 pl-2',
        isLast && 'border-b-0',
      )}
      onClick={() => onChange(value)}
    >
      <Control
        name="currency-select"
        value={value}
        checked={checked}
        onChange={() => onChange(value)}
      />
      <div className="flex w-full justify-between">
        <div className="flex gap-4">
          <img width={28} height={20} src={flagImg} alt={`${country} flag`} />
          <span className="headline1-medium text-label-normal whitespace-nowrap">
            {country}
          </span>
        </div>
        <span className="body1-normal-medium text-label-normal whitespace-nowrap">
          {currency}
        </span>
      </div>
    </div>
  );
};

export default CountryItem;
