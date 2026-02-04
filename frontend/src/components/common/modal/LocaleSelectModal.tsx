import { useState } from 'react';
import { useMatches } from '@tanstack/react-router';
import clsx from 'clsx';

import Control from '@/components/common/Control';
import LocaleConfirmModal from '@/components/common/modal/LocaleConfirmModal';

import { type CountryCode, countryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';

interface CountryItemProps {
  flagImg: string;
  country: string;
  currency: string;
  checked: boolean;
  value: string;
  onChange: (value: string) => void;
  isLast: boolean;
}

const CountryItem = ({
  flagImg,
  country,
  currency,
  checked,
  value,
  onChange,
  isLast,
}: CountryItemProps) => {
  return (
    <div
      className={clsx(
        'border-line-normal-normal flex w-full cursor-pointer items-center justify-center gap-6 border-b py-5 pr-2.5 pl-2',
        isLast && 'border-b-0',
      )}
      onClick={() => onChange(value)}
    >
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
  onSelect?: (code: string) => void;
  selectedCode?: string;
}

const LocaleSelectModal = ({
  mode,
  onSelect,
  selectedCode: propSelectedCode,
}: LocaleSelectModalProps) => {
  const matches = useMatches();
  const isInitPath = matches.some((match) => match.routeId === '/_app/init');
  const [selectedCode, setSelectedCode] = useState<string>(
    propSelectedCode || '',
  );
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);

  const handleSelect = (code: string) => {
    setSelectedCode(code);
    setIsConfirmOpen(true);
  };

  const handleConfirm = () => {
    onSelect?.(selectedCode);
    setIsConfirmOpen(false);
  };

  const handleCancel = () => {
    setSelectedCode('');
    setIsConfirmOpen(false);
  };

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
    <div className="rounded-modal-20 bg-background-normal flex flex-col items-center gap-12 px-7.5 py-13">
      {/* text section */}
      <div className="flex flex-col items-center gap-1.5 text-center">
        <span className="text-label-normal title3-semibold">{title}</span>
        <span className="text-label-alternative heading2-medium">
          {subTitle}
        </span>
      </div>

      {/* select section */}
      <div className="flex flex-col gap-4.5">
        <input
          type="text"
          className="bg-fill-normal rounded-modal-10 h-15 w-full"
        />
        <div className="flex h-142.5 w-118 flex-col overflow-y-auto">
          {countryCode.map((code, index) => {
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
                onChange={handleSelect}
                isLast={index === countryCode.length - 1}
              />
            );
          })}
        </div>
      </div>
      {isConfirmOpen && selectedCode && (
        <LocaleConfirmModal
          isOpen={isConfirmOpen}
          type={mode === 'BASE' ? 'currency' : 'country'}
          code={selectedCode as CountryCode}
          onClose={handleCancel}
          onAction={handleConfirm}
        />
      )}
    </div>
  );
};

export default LocaleSelectModal;
