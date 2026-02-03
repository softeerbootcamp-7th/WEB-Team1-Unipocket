import { useEffect, useState } from 'react';

import { cn } from '@/lib/utils';

import Button from '../common/Button';

const InfiniteCurrency = () => {
  const currencyList = [
    { symbol: '$', code: 'AUD', krw: '₩ 1,009' },
    { symbol: '$', code: 'CAD', krw: '₩ 1,064' },
    { symbol: '$', code: 'USD', krw: '₩ 1,451' },
    { symbol: 'kr', code: 'SEK', krw: '₩ 162' },
    { symbol: 'kr', code: 'NOK', krw: '₩ 150' },
    { symbol: 'Fr', code: 'CHF', krw: '₩ 1,870' },
    { symbol: '€', code: 'EUR', krw: '₩ 1,719' },
    { symbol: '£', code: 'GBP', krw: '₩ 1,987' },
    { symbol: '¥', code: 'JPY', krw: '₩ 9' },
    { symbol: '¥', code: 'CNY', krw: '₩ 209' },
  ];

  // 현재 강조될 버튼의 인덱스 관리
  const [activeIdx, setActiveIdx] = useState(2);
  const [activeLine, setActiveLine] = useState(0); // 0: 첫번째 줄, 1: 두번째 줄

  useEffect(() => {
    const timer = setInterval(() => {
      setActiveIdx((prev) => (prev + 1) % currencyList.length);
      setActiveLine((prev) => (prev === 0 ? 1 : 0)); // 줄을 번갈아 가며 선택
    }, 8000); // 속도에 맞춰 적절히 조절
    return () => clearInterval(timer);
  }, [currencyList.length]);

  // 반복되는 리스트 렌더링 함수
  const renderList = (isReverse = false) => (
    <div
      className={cn(
        isReverse ? 'animate-marquee-reverse' : 'animate-marquee',
        'flex gap-4',
      )}
    >
      {currencyList.map((c, i) => {
        const isSelected = i % currencyList.length === activeIdx;
        return (
          <div key={i} className="relative flex flex-col items-center">
            {isSelected && (
              <div
                className={cn(
                  'absolute flex items-center',
                  !isReverse
                    ? '-top-16 flex-col'
                    : '-bottom-16 flex-col-reverse',
                )}
              >
                <span className="text-primary-normal">{c.krw}</span>
                <div className="bg-primary-normal h-5 w-5 rounded-full" />
                <div className="bg-primary-normal h-5 w-0.5" />
              </div>
            )}

            <Button variant={isSelected ? 'solid' : 'outlined'} size="lg">
              {c.symbol} {c.code}
            </Button>
          </div>
        );
      })}
    </div>
  );

  return (
    <div className="flex flex-col gap-4">
      {renderList(false)}
      {renderList(true)}
    </div>
  );
};

export default InfiniteCurrency;
