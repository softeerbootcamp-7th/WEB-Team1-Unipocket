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

const Money = () => {
  const [localCurrency, setLocalCurrency] = useState('');
  const [baseCurrency, setBaseCurrency] = useState('');
  const [localCurrencyType, setLocalCurrencyType] = useState<number | null>(1);

  const handleLocalChange = (value: string) => {
    const num = Number(value);
    setLocalCurrency(value);

    if (!Number.isNaN(num)) {
      setBaseCurrency(num === 0 ? '' : ((num * RATE).toFixed(0)));
    }
  };

  const handleBaseChange = (value: string) => {
    const num = Number(value);
    setBaseCurrency(value);

    if (!Number.isNaN(num)) {
      setLocalCurrency(num === 0 ? '' : (num / RATE).toFixed(2));
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
            onChange={handleLocalChange}
            className="w-61"
            prefix="$"
          />
          <div className="mt-auto flex w-25">
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
            onChange={handleBaseChange}
            className="w-61"
            prefix="₩"
          />
          <p className="body2-normal-medium text-label-assistive mt-auto ml-2.75 flex h-9 w-20 items-center">
            KRW
          </p>
        </div>
      </div>
    </div>
  );
};

export default Money;
