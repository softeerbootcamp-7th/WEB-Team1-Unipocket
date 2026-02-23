import countryData from '@/data/country/countryData.json';
import { getCountryInfo } from '@/lib/country';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

type CurrencyOption = {
  id: number;
  name: string;
  sign: string;
};

// 중복 제거 (EUR)
const currencyOptions: CurrencyOption[] = Array.from(
  new Map(
    Object.values(countryData).map(({ currencyName, currencySign }) => [
      currencyName,
      currencySign,
    ]),
  ),
).map(([name, sign], index) => ({
  id: index + 1,
  name,
  sign,
}));

const useCurrencyInfo = (
  showCurrencyDropdown: boolean,
  localCurrencyType: number,
) => {
  const { localCountryCode, baseCountryCode } = useRequiredAccountBook();

  const localCountryInfo = getCountryInfo(localCountryCode);
  const baseCountryInfo = getCountryInfo(baseCountryCode);

  const localCurrencyOption = showCurrencyDropdown
    ? currencyOptions.find((o) => o.id === localCurrencyType)
    : undefined;

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
