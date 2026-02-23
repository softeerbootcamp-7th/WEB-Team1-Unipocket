import { useMemo } from 'react';

import countryData from '@/data/country/countryData.json';
import { getCountryInfo } from '@/lib/country';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

type CurrencyOption = {
  id: number;
  name: string;
  sign: string;
};

const currencyOptions: CurrencyOption[] = Object.values(countryData).map(
  (country, index) => ({
    id: index + 1,
    name: country.currencyName,
    sign: country.currencySign,
  }),
);

const useCurrencyInfo = (
  showCurrencyDropdown: boolean,
  localCurrencyType: number,
) => {
  const { localCountryCode, baseCountryCode } = useRequiredAccountBook();

  const localCurrencyOption = useMemo(
    () => currencyOptions.find((o) => o.id === localCurrencyType),
    [localCurrencyType],
  );
  const localCountryInfo = getCountryInfo(localCountryCode);
  const baseCountryInfo = getCountryInfo(baseCountryCode);

  return {
    localCurrencyName: showCurrencyDropdown
      ? localCurrencyOption?.name
      : localCountryInfo?.currencyName,
    localCurrencySign: showCurrencyDropdown
      ? localCurrencyOption?.sign
      : localCountryInfo?.currencySign,
    baseCurrencyName: baseCountryInfo?.currencyName,
    baseCurrencySign: baseCountryInfo?.currencySign,
  };
};

export { currencyOptions };
export default useCurrencyInfo;
