import { useState } from 'react';
import clsx from 'clsx';

import Button from '../common/Button';

interface Currency {
  symbol: string;
  code: string;
  krw: string;
}

const CURRENCY_LIST_1: Currency[] = [
  { symbol: '$', code: 'AUD', krw: '₩ 1,009' },
  { symbol: '$', code: 'CAD', krw: '₩ 1,064' },
  { symbol: '$', code: 'USD', krw: '₩ 1,451' },
  { symbol: 'kr', code: 'SEK', krw: '₩ 162' },
  { symbol: 'kr', code: 'NOK', krw: '₩ 150' },
];

const CURRENCY_LIST_2: Currency[] = [
  { symbol: 'Fr', code: 'CHF', krw: '₩ 1,870' },
  { symbol: '€', code: 'EUR', krw: '₩ 1,719' },
  { symbol: '£', code: 'GBP', krw: '₩ 1,987' },
  { symbol: '¥', code: 'JPY', krw: '₩ 9' },
  { symbol: '¥', code: 'CNY', krw: '₩ 209' },
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

        {/* 동그라미 컨테이너 */}
        <div className="relative mb-0.5 flex h-2 w-2 items-center justify-center">
          {/* 1. 퍼져나가는 효과 (Ping/Ripple) */}
          {isActive && (
            <div className="bg-primary-normal/60 animate-ripple-infinite absolute h-full w-full rounded-full" />
          )}

          {/* 2. 중심에 고정된 작은 동그라미 */}
          <div className="bg-primary-normal relative h-full w-full rounded-full" />
        </div>

        {/* 수직 선 */}
        <div className="bg-primary-normal h-4 w-0.5" />
      </div>

      {/* 버튼 */}
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
  const doubleItems = [...items, ...items];

  return (
    <div className="group">
      <div
        className={clsx(
          'flex gap-3',
          'group-hover:[animation-play-state:paused]',
          reverse ? 'animate-marquee-reverse' : 'animate-marquee',
        )}
      >
        {doubleItems.map((currency, idx) => {
          const uniqueKey = `${currency.code}-${idx}`;
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
    </div>
  );
};

const InfiniteCurrency = () => {
  const [activeCode, setActiveCode] = useState<string | null>(null);

  return (
    <div className="flex w-full flex-col gap-4">
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
