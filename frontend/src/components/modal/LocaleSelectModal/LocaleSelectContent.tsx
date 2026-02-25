import { useMemo, useState } from 'react';
import { toast } from 'sonner';

import LocaleConfirmModal from '@/components/modal/LocaleConfirmModal';
import CountryItem from '@/components/modal/LocaleSelectModal/CountryItem';
import type { LocaleMode } from '@/components/modal/LocaleSelectModal/LocaleSelectModal';
import SearchInput from '@/components/modal/LocaleSelectModal/SearchInput';

import { Icons } from '@/assets';
import { COUNTRY_CODE, type CountryCode } from '@/data/country/countryCode';
import { type CountryInfo, getCountryInfo } from '@/lib/country';

interface LocaleSelectContentProps {
  mode: LocaleMode;
  onSelect?: (code: CountryCode) => void;
  baseCountryCode: CountryCode | null;
  localCountryCode: CountryCode | null;
  isLoading?: boolean;
}

const TOAST_MESSAGE = {
  BASE: '현지 통화와 동일한 통화로 설정할 수 없습니다.',
  LOCAL: '기준 통화와 동일한 통화로 설정할 수 없습니다.',
  INIT: '기준 통화와 동일한 통화로 설정할 수 없습니다.',
} as const;

const CAUTION_MESSAGE = '통화 변경 작업은 최대 5분 정도 소요될 수 있습니다.';

const MODAL_TEXT = {
  BASE: {
    title: '기준 통화 변경',
    subTitle: '선택된 통화로 가계부의 통합 계산 기준이 됩니다',
  },
  LOCAL: {
    title: '국가/통화 변경',
    subTitle: '선택된 나라를 기준으로 현지통화가 설정됩니다',
  },
  INIT: {
    title: '교환학생 가는 나라는 어디신가요?',
    subTitle: '선택된 나라를 기준으로 현지통화가 설정됩니다',
  },
} as const;

const LocaleSelectContent = ({
  mode,
  onSelect,
  baseCountryCode,
  localCountryCode,
  isLoading,
}: LocaleSelectContentProps) => {
  const initialSelectedCode =
    mode === 'BASE' ? baseCountryCode : localCountryCode;
  const oppositeCode = mode === 'BASE' ? localCountryCode : baseCountryCode;

  const [selectedCode, setSelectedCode] = useState<CountryCode | null>(
    initialSelectedCode,
  );
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [keyword, setKeyword] = useState('');

  const allCountries = useMemo(
    () =>
      Object.values(COUNTRY_CODE).reduce<
        {
          countryCode: CountryCode;
          data: CountryInfo;
        }[]
      >((acc, countryCode) => {
        const data = getCountryInfo(countryCode);
        if (data) acc.push({ countryCode, data });
        return acc;
      }, []),
    [],
  );

  const filteredCountries = useMemo(() => {
    const trimmed = keyword.trim().toLowerCase();
    return allCountries.filter(
      ({ countryCode, data }) =>
        !trimmed ||
        data.countryName.toLowerCase().includes(trimmed) ||
        data.currencyName.toLowerCase().includes(trimmed) ||
        countryCode.toLowerCase().includes(trimmed),
    );
  }, [keyword, allCountries]);

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

  const { title, subTitle } = MODAL_TEXT[mode];

  return (
    <div className="rounded-t-modal-20 bg-background-normal relative flex h-full flex-col items-center gap-12 px-7.5 py-13">
      {isLoading && (
        <div className="rounded-t-modal-20 absolute inset-0 z-10 flex items-center justify-center bg-black/40">
          <Icons.Loading className="text-inverse-label size-5.5 animate-spin" />
        </div>
      )}
      {/* text section */}
      <div className="flex flex-col items-center gap-1.5 text-center">
        <span className="text-label-normal title3-semibold">{title}</span>
        <span className="text-label-alternative heading2-medium">
          {subTitle}
        </span>
        {mode !== 'INIT' && (
          <span className="text-status-cautionary body1-normal-medium">
            {CAUTION_MESSAGE}
          </span>
        )}
      </div>

      {/* select section */}
      <div className="flex h-full flex-col gap-4.5">
        <SearchInput value={keyword} onChange={setKeyword} />
        <div className="flex flex-1 flex-col overflow-y-auto pb-50 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
          {filteredCountries.map((item, index, arr) => {
            const { countryCode, data } = item;

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
          {filteredCountries.length === 0 && (
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

export default LocaleSelectContent;
