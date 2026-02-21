import { useMemo, useState } from 'react';
import { useMatchRoute } from '@tanstack/react-router';
import clsx from 'clsx';
import { toast } from 'sonner';

import Control from '@/components/common/Control';
import LocaleConfirmModal from '@/components/modal/LocaleConfirmModal';

import type { CurrencyType } from '@/types/currency';

import { Icons } from '@/assets';
import { COUNTRY_CODE, type CountryCode } from '@/data/country/countryCode';
import { getCountryInfo } from '@/lib/country';

interface CountryItemProps {
  flagImg: string;
  country: string;
  currency: string;
  checked: boolean;
  value: CountryCode;
  onChange: (value: CountryCode) => void;
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
        'border-line-normal-normal flex w-118 cursor-pointer items-center justify-center gap-6 border-b py-5 pr-2.5 pl-2',
        isLast && 'border-b-0',
      )}
      onClick={() => onChange(value)}
    >
      <Control
        name="currency-select"
        value={value}
        checked={checked}
        onChange={() => onChange(value)}
      />
      <div className="flex w-full justify-between">
        <div className="flex gap-4">
          <img width={28} height={20} src={flagImg} alt={`${country} flag`} />
          <span className="headline1-medium text-label-normal whitespace-nowrap">
            {country}
          </span>
        </div>
        <span className="body1-normal-medium text-label-normal whitespace-nowrap">
          {currency}
        </span>
      </div>
    </div>
  );
};

interface LocaleSelectModalProps {
  mode: CurrencyType;
  onSelect?: (code: CountryCode) => void;
  baseCountryCode: CountryCode | null;
  localCountryCode: CountryCode | null;
}

const TOAST_MESSAGE = {
  BASE: '현지 통화와 동일한 통화로 설정할 수 없습니다.',
  LOCAL: '기준 통화와 동일한 통화로 설정할 수 없습니다.',
} as const;

/**
 * 기준 통화 변경 시 mode = 'BASE', 현지 통화 변경 시 mode = 'LOCAL'로 설정
 */
const LocaleSelectModal = ({
  mode,
  onSelect,
  baseCountryCode,
  localCountryCode,
}: LocaleSelectModalProps) => {
  const matchRoute = useMatchRoute();
  const isInitPath = !!matchRoute({ to: '/init' });

  const initialSelectedCode =
    mode === 'BASE' ? baseCountryCode : localCountryCode;
  const oppositeCode = mode === 'BASE' ? localCountryCode : baseCountryCode;

  const [selectedCode, setSelectedCode] = useState<CountryCode | null>(
    initialSelectedCode,
  );
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [keyword, setKeyword] = useState('');

  const filteredCountryCodes = useMemo(() => {
    if (!keyword.trim()) return Object.values(COUNTRY_CODE);

    return Object.values(COUNTRY_CODE).filter((countryCode) => {
      const data = getCountryInfo(countryCode);
      if (!data) return false;

      const lowerKeyword = keyword.toLowerCase();

      return (
        data.countryName.toLowerCase().includes(lowerKeyword) ||
        data.currencyName.toLowerCase().includes(lowerKeyword) ||
        countryCode.toLowerCase().includes(lowerKeyword)
      );
    });
  }, [keyword]);

  const handleSelect = (code: CountryCode) => {
    if (code === oppositeCode) {
      toast.error(TOAST_MESSAGE[mode]);
      return;
    }

    setSelectedCode(code);
    setIsConfirmOpen(true);
  };

  const handleConfirm = () => {
    if (selectedCode !== null) {
      onSelect?.(selectedCode);
    }
    setIsConfirmOpen(false);
  };

  const handleCancel = () => {
    setSelectedCode(initialSelectedCode);
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
      <div className="flex h-full flex-col gap-4.5">
        <div className="bg-fill-normal rounded-modal-10 flex h-14.25 w-full items-center gap-[4.8px] p-[14.4px]">
          <Icons.Search className="text-line-normal-normal size-5" />
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="검색어를 입력해주세요."
            className="placeholder:text-label-assistive placeholder:headline1-medium w-full focus:outline-none"
          />
        </div>
        <div className="flex flex-1 flex-col overflow-y-auto pb-50 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
          {filteredCountryCodes.map((countryCode, index, arr) => {
            const data = getCountryInfo(countryCode);
            if (!data) return null;

            return (
              <CountryItem
                key={countryCode}
                value={countryCode}
                flagImg={data.imageUrl}
                country={data.countryName}
                currency={`${data.currencySign} ${data.currencyName}`}
                checked={selectedCode === countryCode}
                onChange={handleSelect}
                isLast={index === arr.length - 1}
              />
            );
          })}
          {filteredCountryCodes.length === 0 && (
            <div className="headline1-medium text-label-assistive w-118 px-4 py-6 text-center">
              검색 결과가 없습니다.
            </div>
          )}
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
