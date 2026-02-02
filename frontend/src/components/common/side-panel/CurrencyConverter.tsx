import { useState } from 'react';

import Divider from '@/components/common/Divider';
import DropDown from '@/components/common/dropdown/Dropdown';
import TextInput from '@/components/common/TextInput';

import { Icons } from '@/assets';

interface CurrencyOption {
  id: number;
  name: string;
}

const currencyOptions: CurrencyOption[] = [
  { id: 1, name: 'USD' },
  { id: 2, name: 'KRW' },
  { id: 3, name: 'JPY' },
  { id: 4, name: 'EUR' },
];

// 환율 고정 (API 연동 후 선택 가능하도록 변경 예정)
const RATE = 1464; // USD -> KRW

const CurrencyConverter = () => {
  const [localCurrency, setLocalCurrency] = useState('');
  const [baseCurrency, setBaseCurrency] = useState('');
  const [localCurrencyType, setLocalCurrencyType] = useState(1);
  const [amountError, setAmountError] = useState<string | null>('');

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
    <div className="flex w-90 flex-col gap-6">
      <div className="flex flex-col gap-1.5">
        <p className="headline2-bold text-label-normal h-8">금액</p>
        <p className="body2-normal-medium text-label-alternative whitespace-pre-line">
          {'현지 금액이나 기준 금액 중 하나만 입력하면\n자동으로 환산돼요.'}
        </p>
      </div>

      <div className="flex flex-col gap-3">
        <div className="flex gap-4">
          <TextInput
            title="현지 금액"
            value={localCurrency}
            placeholder="0"
            onChange={(value) => handleCurrencyChange(value, 'toBase')}
            className="w-61"
            prefix="$"
            isError={!!amountError}
            errorMessage={amountError ?? undefined}
          />
          <div className="flex w-25 pt-7">
            <DropDown
              selected={localCurrencyType}
              onSelect={setLocalCurrencyType}
              options={currencyOptions}
              size="lg"
            />
          </div>
        </div>

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
            className="w-61"
            prefix="₩"
            isError={!!amountError}
            errorMessage={amountError ?? undefined}
          />
          <p className="body2-normal-medium text-label-assistive ml-2.75 w-20 items-center pt-10.5">
            KRW
          </p>
        </div>
      </div>
    </div>
  );
};

export default CurrencyConverter;
