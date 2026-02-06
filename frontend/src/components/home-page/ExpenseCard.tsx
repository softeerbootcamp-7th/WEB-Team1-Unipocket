import { Icons } from '@/assets';
import type { CountryCode } from '@/data/countryCode';

import CurrencyAmountDisplay from '../common/CurrencyAmountDisplay';
import CurrencyBadge from '../common/CurrencyBadge';

interface ExpenseCardProps {
  label: string;
  localCountryCode: CountryCode; // 현지통화
  localCountryAmount: number; // 현지금액
  baseCountryCode: CountryCode; // 기준통화
  baseCountryAmount: number; // 기준금액
}

const ExpenseCard = ({
  label,
  localCountryCode,
  localCountryAmount,
  baseCountryCode,
  baseCountryAmount,
}: ExpenseCardProps) => {
  return (
    <section className="flex flex-col gap-2">
      <div className="text-label-alternative flex items-center gap-1.5">
        <span className="body2-normal-medium">{label}</span>
        <Icons.Information className="size-4" />
      </div>
      <div className="flex items-end gap-3">
        <CurrencyAmountDisplay
          countryCode={baseCountryCode}
          amount={baseCountryAmount}
          size={'lg'}
        />
        <div className="flex gap-1.5 py-0.75">
          <CurrencyBadge countryCode={localCountryCode} />
          <CurrencyAmountDisplay
            countryCode={localCountryCode}
            amount={localCountryAmount}
            size={'sm'}
          />
        </div>
      </div>
    </section>
  );
};

export default ExpenseCard;
