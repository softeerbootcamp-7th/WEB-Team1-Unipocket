import { clsx } from 'clsx';

import type { CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

import type { CountryCode } from '@/data/countryCode';

import CurrencyAmountDisplay from '../../currency/CurrencyAmountDisplay';
import CurrencyBadge from '../../currency/CurrencyBadge';

interface CategoryLegendItemProps {
  currencyType: CurrencyType;
  countryCode: CountryCode;
  categoryName: CategoryType;
  percentage: number;
  amount: number;
  color: string;
}

const CategoryLegendItem = ({
  currencyType,
  countryCode,
  categoryName,
  percentage,
  amount,
  color,
}: CategoryLegendItemProps) => {
  return (
    <div className="flex h-6 w-full items-center gap-4">
      <div className="h-3.5 w-3.5" style={{ backgroundColor: color }} />
      <div className="flex items-center gap-2.5">
        <span className="label1-normal-medium text-label-normal min-w-9">
          {categoryName}
        </span>
        <span className="figure-body2-14-semibold text-label-alternative min-w-7.5 text-end">
          {percentage}%
        </span>
      </div>
      <div className="flex min-w-25 flex-1 items-center justify-start gap-1">
        <div className={clsx(currencyType === 'BASE' && 'opacity-0')}>
          <CurrencyBadge countryCode={countryCode} />
        </div>

        <div className="max-w-25 truncate">
          <CurrencyAmountDisplay
            countryCode={countryCode}
            amount={amount}
            size="sm"
            className="text-label-neutral"
          />
        </div>
      </div>
    </div>
  );
};

export default CategoryLegendItem;
