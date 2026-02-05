import type { CountryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';

interface CurrencyBadgeProps {
  countryCode: CountryCode;
}

const CurrencyBadge = ({ countryCode }: CurrencyBadgeProps) => {
  const countryInfo = getCountryInfo(countryCode);

  if (!countryInfo) return null;

  return (
    <div className="flex items-center gap-1">
      <img
        className="h-3 w-4"
        src={countryInfo.imageUrl}
        alt={`${countryInfo.currencyName} flag`}
      />
      <span className="text-label-neutral text-[13px]">
        {countryInfo.currencyName}
      </span>
    </div>
  );
};

export default CurrencyBadge;
