import { useState } from 'react';
import clsx from 'clsx';

import Button from '../common/Button';

interface Currency {
  symbol: string;
  code: string;
  krw: string;
}

const CURRENCY_LIST_1: Currency[] = [
  { symbol: '$', code: 'USD', krw: '₩ 1,451' }, // 미국
  { symbol: '€', code: 'EUR', krw: '₩ 1,519' }, // 유럽연합
  { symbol: '£', code: 'GBP', krw: '₩ 1,787' }, // 영국
  { symbol: '¥', code: 'JPY', krw: '₩ 9.45' }, // 일본 (100엔 기준이 아닌 1엔 단위 표기 시)
  { symbol: '¥', code: 'CNY', krw: '₩ 198' }, // 중국
  { symbol: '$', code: 'AUD', krw: '₩ 942' }, // 호주
  { symbol: '$', code: 'CAD', krw: '₩ 1,034' }, // 캐나다
  { symbol: '$', code: 'SGD', krw: '₩ 1,075' }, // 싱가포르
  { symbol: 'CHF', code: 'CHF', krw: '₩ 1,640' }, // 스위스
  { symbol: '$', code: 'HKD', krw: '₩ 185' }, // 홍콩
];

const CURRENCY_LIST_2: Currency[] = [
  { symbol: '฿', code: 'THB', krw: '₩ 41.2' }, // 태국
  { symbol: '₫', code: 'VND', krw: '₩ 0.057' }, // 베트남
  { symbol: '$', code: 'TWD', krw: '₩ 44.8' }, // 대만
  { symbol: '₱', code: 'PHP', krw: '₩ 25.4' }, // 필리핀
  { symbol: 'RM', code: 'MYR', krw: '₩ 325' }, // 말레이시아
  { symbol: 'kr', code: 'SEK', krw: '₩ 134' }, // 스웨덴
  { symbol: 'kr', code: 'NOK', krw: '₩ 132' }, // 노르웨이
  { symbol: 'kr', code: 'DKK', krw: '₩ 203' }, // 덴마크
  { symbol: 'NZ$', code: 'NZD', krw: '₩ 854' }, // 뉴질랜드
  { symbol: 'Rp', code: 'IDR', krw: '₩ 0.091' }, // 인도네시아
];

interface CurrencyItemProps {
  currency: Currency;
  isActive: boolean;
  isReverse: boolean;
  onSelect: (code: string) => void;
}

const CurrencyItem = ({
  currency,
  isActive,
  isReverse,
  onSelect,
}: CurrencyItemProps) => {
  return (
    <div
      className="relative flex shrink-0 flex-col items-center"
      onMouseEnter={() => onSelect(currency.code)}
    >
      <div
        className={clsx(
          'absolute flex flex-col items-center transition-opacity duration-300',
          isActive ? 'z-10 opacity-100' : 'z-0 opacity-0',
          !isReverse ? '-top-16' : '-bottom-16 flex-col-reverse',
        )}
      >
        <span className="text-primary-normal mb-1 font-bold whitespace-nowrap">
          {currency.krw}
        </span>
        <div className="relative mb-0.5 flex h-2 w-2 items-center justify-center">
          {isActive && (
            <div className="bg-primary-normal/60 animate-ripple absolute h-full w-full rounded-full" />
          )}
          <div className="bg-primary-normal relative h-full w-full rounded-full" />
        </div>
        <div className="bg-primary-normal h-7 w-0.5" />
      </div>

      <Button
        variant={isActive ? 'solid' : 'outlined'}
        size="lg"
        onClick={() => onSelect(currency.code)}
      >
        {currency.symbol} {currency.code}
      </Button>
    </div>
  );
};

interface MarqueeRowProps {
  items: Currency[];
  activeCode: string | null;
  onSelect: (code: string) => void;
  reverse: boolean;
}

const MarqueeRow = ({
  items,
  activeCode,
  onSelect,
  reverse,
}: MarqueeRowProps) => {
  const repeatedItems = [...items];

  const renderTrack = (trackIndex: number) => (
    <div
      className={clsx(
        'flex shrink-0 gap-3 pr-3', // pr-3는 두 트랙 사이의 간격 유지를 위함
        'group-hover:[animation-play-state:paused]',
        reverse ? 'animate-marquee-reverse' : 'animate-marquee',
      )}
      aria-hidden={trackIndex === 1} // 접근성을 위해 두 번째 트랙은 숨김 처리
    >
      {repeatedItems.map((currency, idx) => {
        const uniqueKey = `${trackIndex}-${currency.code}-${idx}`;
        const isActive = activeCode === currency.code;
        return (
          <CurrencyItem
            key={uniqueKey}
            currency={currency}
            isActive={isActive}
            isReverse={reverse}
            onSelect={onSelect}
          />
        );
      })}
    </div>
  );

  return (
    <div className="group flex">
      {/* 두 개의 트랙을 연속 배치하여 무한 롤링 효과 구현 */}
      {renderTrack(0)}
      {renderTrack(1)}
    </div>
  );
};

const InfiniteCurrency = () => {
  const [activeCode, setActiveCode] = useState('JPY');

  return (
    <div className="flex w-full flex-col gap-3">
      <MarqueeRow
        items={CURRENCY_LIST_1}
        activeCode={activeCode}
        onSelect={setActiveCode}
        reverse={false}
      />
      <MarqueeRow
        items={CURRENCY_LIST_2}
        activeCode={activeCode}
        onSelect={setActiveCode}
        reverse={true}
      />
    </div>
  );
};

export default InfiniteCurrency;
