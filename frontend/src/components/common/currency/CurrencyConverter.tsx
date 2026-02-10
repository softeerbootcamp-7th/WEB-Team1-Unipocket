import { useContext, useEffect, useState } from 'react';

import Divider from '@/components/common/Divider';
import DropDown from '@/components/common/dropdown/Dropdown';
import { ModalContext } from '@/components/common/modal/useModalContext';
import TextInput from '@/components/common/TextInput';

import { Icons } from '@/assets';
import countryData from '@/data/countryData.json';

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

// 환율 고정 (API 연동 후 선택 가능하도록 변경 예정)
const RATE = 1464; // USD -> KRW

interface CurrencyConverterProps {
  showCurrencyDropdown?: boolean;
}

const CurrencyConverter = ({
  showCurrencyDropdown = false,
}: CurrencyConverterProps) => {
  const modalContext = useContext(ModalContext);

  const [localCurrency, setLocalCurrency] = useState('');
  const [baseCurrency, setBaseCurrency] = useState('');
  const [localCurrencyType, setLocalCurrencyType] = useState(13); // USD. 실제값으로 변경 필요
  const [amountError, setAmountError] = useState<string | null>('');

  useEffect(() => {
    if (!modalContext) return;
    const hasValidInput =
      localCurrency !== '' && baseCurrency !== '' && !amountError;
    modalContext.setActionReady(hasValidInput);
  }, [localCurrency, baseCurrency, amountError, modalContext]);

  const validateNumber = (value: string) => {
    const sanitized = value.replace(/[^0-9.]/g, '');
    const isValid =
      value === sanitized && (sanitized.match(/\./g)?.length ?? 0) <= 1;
    return { sanitized, isValid };
  };

  const handleCurrencyChange = (
    value: string,
    direction: 'toBase' | 'toLocal',
  ) => {
    const { sanitized, isValid } = validateNumber(value);

    if (!isValid) {
      setAmountError('숫자만 입력할 수 있어요.');
      return;
    }

    setAmountError(null);
    const num = Number(sanitized);

    if (direction === 'toBase') {
      setLocalCurrency(sanitized);
      setBaseCurrency(
        sanitized === ''
          ? ''
          : Number((num * RATE).toFixed(0)).toLocaleString(),
      );
    } else {
      setBaseCurrency(sanitized);
      setLocalCurrency(
        sanitized === ''
          ? ''
          : Number((num / RATE).toFixed(2)).toLocaleString(undefined, {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            }),
      );
    }
  };

  return (
    <div className="flex flex-col gap-3">
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
              selected={localCurrencyType}
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
              USD 1 = KRW 1,464
            </p>
            <p className="label1-normal-medium text-label-alternative">
              자동 환율 적용 중 (2025.12.02. 13:26 기준)
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
            KRW
          </p>
        )}
      </div>
    </div>
  );
};

export default CurrencyConverter;
