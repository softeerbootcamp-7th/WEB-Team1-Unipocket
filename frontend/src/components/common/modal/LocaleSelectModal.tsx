import { useState } from 'react';
import { useMatches } from '@tanstack/react-router';

import Control from '@/components/common/Control';

import { countryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';

interface CountryItemProps {
  flagImg: string;
  country: string;
  currency: string;
  checked: boolean;
  value: string;
  onChange: (value: string) => void;
}

const CountryItem = ({
  flagImg,
  country,
  currency,
  checked,
  value,
  onChange,
}: CountryItemProps) => {
  return (
    <div className="border-line-normal-normal flex w-full items-center justify-center gap-6 border-b py-5 pr-2.5 pl-2">
      <Control
        name="currency-select"
        value={value}
        checked={checked}
        onChange={onChange}
      />
      <div className="flex w-full justify-between">
        <div className="flex gap-4">
          <img src={flagImg} alt={`${country} flag`} />
          <span>{country}</span>
        </div>
        <span>{currency}</span>
      </div>
    </div>
  );
};

type LocaleSelectMode = 'BASE' | 'LOCAL'; // 기준 통화, 현지 통화

interface LocaleSelectModalProps {
  mode: LocaleSelectMode;
}

const LocaleSelectModal = ({ mode }: LocaleSelectModalProps) => {
  const matches = useMatches();
  const isInitPath = matches.some((match) => match.routeId === '/_app/init');
  const [selectedCode, setSelectedCode] = useState<string>('');

  const getTextContent = () => {
    if (mode === 'BASE') {
      return {
        title: '기준 통화 변경',
        subTitle: '선택된 통화로 가계부의 통합 계산 기준이 됩니다',
      };
    }

    // LOCAL mode
    return {
      title: isInitPath ? '교환학생 가는 나라는 어디신가요?' : '국가/통화 변경',
      subTitle: '선택된 나라를 기준으로 현지통화가 설정됩니다',
    };
  };

  const { title, subTitle } = getTextContent();

  return (
    <div className="rounded-modal-20 bg-background-normal flex h-221 w-133 flex-col items-center gap-12 px-7.5 py-13">
      {/* text section */}
      <div className="flex flex-col items-center gap-1.5 text-center">
        <span className="text-label-normal title3-semibold">{title}</span>
        <span className="text-label-assistive heading2-medium">{subTitle}</span>
      </div>

      {/* select section */}
      <div>
        <input type="text" />
        <div className="flex h-142.5 w-118 flex-col overflow-y-auto">
          {countryCode.map((code) => {
            const data = getCountryInfo(code);

            if (!data) return null;

            return (
              <CountryItem
                key={code}
                value={code}
                flagImg={data.imageUrl}
                country={data.countryName}
                currency={`${data.currencySign} ${data.currencyName}`}
                checked={selectedCode === code}
                onChange={setSelectedCode}
              />
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default LocaleSelectModal;
