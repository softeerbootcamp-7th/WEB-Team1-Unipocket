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

export type CurrencyValues = {
  localAmount: number;
  localCurrencyCode: CurrencyCode;
  baseAmount: number;
};

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

const DEFAULT_LOCAL_CURRENCY_TYPE = 13; // USD. 실제값으로 변경 필요

// 환율 고정 (API 연동 후 선택 가능하도록 변경 예정)
const RATE = 1464; // USD -> KRW

interface CurrencyConverterProps {
  showCurrencyDropdown?: boolean;
  rateUpdatedAt?: Date;
  onValuesChange?: (values: CurrencyValues) => void;
  onBaseCurrencyChange?: (amount: number) => void;
}

const formatRateDate = (date: Date): string => {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}. ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const CurrencyConverter = ({
  showCurrencyDropdown = false,
  rateUpdatedAt,
  onValuesChange,
  onBaseCurrencyChange,
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

  const localCurrencyName = currencyOptions.find(
    (o) => o.id === localCurrencyType,
  )?.name;
  const localCurrencySign = currencyOptions.find(
    (o) => o.id === localCurrencyType,
  )?.sign;

  const baseCountryInfo = getCountryInfo(
    useRequiredAccountBook().baseCountryCode,
  );
  const baseCurrencyName = baseCountryInfo?.currencyName;
  const baseCurrencySign = baseCountryInfo?.currencySign;

  useEffect(() => {
    if (modalContext) {
      modalContext.setActionReady(isValid);
    }
  }, [isValid, modalContext]);

  useEffect(() => {
    if (!onValuesChange || !isValid || !localCurrencyName) return;
    const toNumber = (s: string) => parseFloat(s.replace(/,/g, ''));
    onValuesChange({
      localAmount: toNumber(localCurrency),
      localCurrencyCode: localCurrencyName as CurrencyCode,
      baseAmount: toNumber(baseCurrency),
    });
  }, [localCurrency, baseCurrency, localCurrencyName, isValid, onValuesChange]);

  useEffect(() => {
    if (onBaseCurrencyChange && baseCurrency) {
      const numericValue = Number(baseCurrency.replace(/[^0-9.]+/g, ''));
      onBaseCurrencyChange(isNaN(numericValue) ? 0 : numericValue);
    }
  }, [baseCurrency, onBaseCurrencyChange]);

  return (
    <div className="flex h-62 flex-col gap-3">
      <div className="flex gap-4">
        <TextInput
          title="현지 금액"
          value={localCurrency}
          placeholder="0"
          onChange={(value) => handleCurrencyChange(value, 'toBase')}
          className="flex-1"
          prefix={localCurrencySign}
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

      <div className="flex flex-col">
        <div className="px-5">
          <Divider style="vertical" className="h-3" />
        </div>
        <div className="flex h-11 items-center gap-2.5 pl-3.25">
          <Icons.Swap className="text-label-neutral h-4 w-4" />
          <div className="flex flex-col gap-1">
            <p className="label1-normal-medium text-label-normal">
              {localCurrencyName} 1 = {baseCurrencyName} {RATE.toLocaleString()}
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
          prefix={baseCurrencySign}
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
