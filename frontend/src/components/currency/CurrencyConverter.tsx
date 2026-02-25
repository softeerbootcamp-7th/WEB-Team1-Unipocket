import { useContext, useEffect, useMemo, useState } from 'react';
import { toast } from 'sonner';

import { useAutoFitScale } from '@/hooks/useAutoFitScale';

import Divider from '@/components/common/Divider';
import DropDown from '@/components/common/dropdown/Dropdown';
import TextInput from '@/components/common/TextInput';
import useCurrencyConverter from '@/components/currency/useCurrencyConverter';
import useCurrencyInfo, {
  currencyOptions,
} from '@/components/currency/useCurrencyInfo';
import { ModalContext } from '@/components/modal/useModalContext';

import { useGetExchangeRateQuery } from '@/api/account-books/query';
import { Icons } from '@/assets';
import countryData from '@/data/country/countryData.json';
import type { CurrencyCode } from '@/data/country/currencyCode';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

export type CurrencyValues = {
  localAmount: number;
  localCurrencyCode: CurrencyCode;
  baseAmount: number;
};

const toNumber = (s: string) => parseFloat(s.replace(/,/g, ''));

interface CurrencyConverterProps {
  showCurrencyDropdown?: boolean;
  rateUpdatedAt?: Date;
  onValuesChange?: (values: CurrencyValues) => void;
  onBaseCurrencyChange?: (amount: number) => void;
  resetTrigger?: number;
}

const formatRateDate = (date: Date): string => {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}. ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const SIGN_MAX_WIDTH = 20;

const ScaledSign = ({ sign }: { sign?: string }) => {
  const { ref, scale } = useAutoFitScale<HTMLSpanElement>(SIGN_MAX_WIDTH, [
    sign,
  ]);
  if (!sign) return null;
  return (
    <span
      ref={ref}
      style={{
        display: 'inline-block',
        transformOrigin: 'left center',
        transform: `scale(${scale})`,
      }}
    >
      {sign}
    </span>
  );
};

const CurrencyConverter = ({
  showCurrencyDropdown = false,
  rateUpdatedAt,
  onValuesChange,
  onBaseCurrencyChange,
  resetTrigger,
}: CurrencyConverterProps) => {
  const rateDate = formatRateDate(rateUpdatedAt ?? new Date());
  const modalContext = useContext(ModalContext);
  const { localCountryCode } = useRequiredAccountBook();
  const [localCurrencyType, setLocalCurrencyType] = useState(() => {
    const idx = Object.keys(countryData).indexOf(localCountryCode);
    return idx >= 0 ? idx + 1 : 1;
  });

  const {
    localCurrencyName,
    localCurrencySign,
    baseCurrencyName,
    baseCurrencySign,
  } = useCurrencyInfo(showCurrencyDropdown, localCurrencyType);

  const occurredAt = useMemo(
    () => rateUpdatedAt?.toISOString() ?? new Date().toISOString(),
    [rateUpdatedAt],
  );
  const { data: exchangeRateData } = useGetExchangeRateQuery(
    occurredAt,
    localCurrencyName as CurrencyCode,
    baseCurrencyName as CurrencyCode,
  );
  const rate = exchangeRateData?.exchangeRate;

  const {
    localCurrency,
    baseCurrency,
    localError,
    baseError,
    handleCurrencyChange,
    isValid,
    reset,
  } = useCurrencyConverter(rate ?? 0);

  useEffect(() => {
    if (!resetTrigger) return;
    reset();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resetTrigger]);

  const setActionReady = modalContext?.setActionReady;

  const handleCurrencySelect = (id: number) => {
    const selected = currencyOptions.find((o) => o.id === id);

    if (selected?.name === baseCurrencyName) {
      toast.error('기준 통화와 동일한 통화는 선택할 수 없습니다.');
      return;
    }

    setLocalCurrencyType(id);
  };

  useEffect(() => {
    setActionReady?.(isValid);
  }, [isValid, setActionReady]);

  useEffect(() => {
    if (!onValuesChange || !isValid || !localCurrencyName) return;
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
          prefix={<ScaledSign sign={localCurrencySign} />}
          isError={!!localError}
          errorMessage={localError ?? undefined}
        />
        {showCurrencyDropdown && (
          <div className="flex w-25 pt-7">
            <DropDown
              selectedId={localCurrencyType}
              onSelect={handleCurrencySelect}
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
              {!rate
                ? '환율 정보 없음'
                : `${localCurrencyName} 1 = ${baseCurrencyName} ${rate.toLocaleString()}`}
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
          prefix={<ScaledSign sign={baseCurrencySign} />}
          isError={!!baseError}
          errorMessage={baseError ?? undefined}
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
