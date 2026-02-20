import { useContext, useEffect, useState } from 'react';

import Divider from '@/components/common/Divider';
import DropDown from '@/components/common/dropdown/Dropdown';
import TextInput from '@/components/common/TextInput';
import useCurrencyConverter from '@/components/currency/useCurrencyConverter';
import { ModalContext } from '@/components/modal/useModalContext';

import { Icons } from '@/assets';
import countryData from '@/data/country/countryData.json';
import type { CurrencyCode } from '@/data/country/currencyCode';
import { getCountryInfo } from '@/lib/country';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

export interface CurrencyValues {
  localAmount: number;
  localCurrencyCode: CurrencyCode;
  baseAmount: number;
}

interface CurrencyOption {
  id: number;
  name: string;
}

const currencyOptions: CurrencyOption[] = Object.values(countryData).map(
  (country, index) => ({
    id: index + 1,
    name: country.currencyName,
  }),
);

const DEFAULT_LOCAL_CURRENCY_TYPE = 13; // USD. 실제값으로 변경 필요

// 환율 고정 (API 연동 후 선택 가능하도록 변경 예정)
const RATE = 1464; // USD -> KRW

interface CurrencyConverterProps {
  showCurrencyDropdown?: boolean;
  rateUpdatedAt?: Date;
  onValuesChange?: (values: CurrencyValues) => void;
}

const formatRateDate = (date: Date): string => {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}. ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const CurrencyConverter = ({
  showCurrencyDropdown = false,
  rateUpdatedAt,
  onValuesChange,
}: CurrencyConverterProps) => {
  const rateDate = formatRateDate(rateUpdatedAt ?? new Date());
  const modalContext = useContext(ModalContext);
  const {
    localCurrency,
    baseCurrency,
    amountError,
    handleCurrencyChange,
    isValid,
  } = useCurrencyConverter(RATE);
  const [localCurrencyType, setLocalCurrencyType] = useState(
    DEFAULT_LOCAL_CURRENCY_TYPE,
  );

  const localCurrencyName =
    currencyOptions.find((o) => o.id === localCurrencyType)?.name ?? '';

  const baseCountryCode = useRequiredAccountBook().baseCountryCode;

  const baseCurrencyName = baseCountryCode
    ? (getCountryInfo(baseCountryCode)?.currencyName ?? 'KRW')
    : 'KRW';

  useEffect(() => {
    if (modalContext) {
      modalContext.setActionReady(isValid);
    }
  }, [isValid, modalContext]);

  useEffect(() => {
    if (!onValuesChange || !isValid || !localCurrencyName) return;
    const parse = (s: string) => parseFloat(s.replace(/,/g, ''));
    onValuesChange({
      localAmount: parse(localCurrency),
      localCurrencyCode: localCurrencyName as CurrencyCode,
      baseAmount: parse(baseCurrency),
    });
  }, [localCurrency, baseCurrency, localCurrencyName, isValid, onValuesChange]);

  return (
    <div className="flex h-62 flex-col gap-3">
      <div className="flex gap-4">
        <TextInput
          title="현지 금액"
          value={localCurrency}
          placeholder="0"
          onChange={(value) => handleCurrencyChange(value, 'toBase')}
          className="flex-1"
          prefix="$"
          isError={!!amountError}
          errorMessage={amountError ?? undefined}
        />
        {showCurrencyDropdown && (
          <div className="flex w-25 pt-7">
            <DropDown
              selectedId={localCurrencyType}
              onSelect={setLocalCurrencyType}
              options={currencyOptions}
              size="lg"
              itemWidth="w-25"
            />
          </div>
        )}
      </div>

      {/* TODO: API 연동 시 해당 수정 필요 */}
      <div className="flex flex-col">
        <div className="px-5">
          <Divider style="vertical" className="h-3" />
        </div>
        <div className="flex h-11 items-center gap-2.5 pl-3.25">
          <Icons.Swap className="text-label-neutral h-4 w-4" />
          <div className="flex flex-col gap-1">
            <p className="label1-normal-medium text-label-normal">
              {localCurrencyName} 1 = {baseCurrencyName} 1,464
            </p>
            <p className="label1-normal-medium text-label-alternative">
              자동 환율 적용 중 ({rateDate} 기준)
            </p>
          </div>
        </div>
        <div className="px-5">
          <Divider style="vertical" className="h-3" />
        </div>
      </div>

      <div className="flex gap-4">
        <TextInput
          title="기준 금액"
          value={baseCurrency}
          placeholder="0"
          onChange={(value) => handleCurrencyChange(value, 'toLocal')}
          className="flex-1"
          prefix="₩"
          isError={!!amountError}
          errorMessage={amountError ?? undefined}
        />
        {showCurrencyDropdown && (
          <p className="body2-normal-medium text-label-assistive ml-2.75 w-20 items-center pt-10.5">
            {baseCurrencyName}
          </p>
        )}
      </div>
    </div>
  );
};

export default CurrencyConverter;
