import { useState } from 'react';
import { clsx } from 'clsx';

import Switch from '@/components/common/Switch';
import ReportCategory from '@/components/report-page/category/ReportCategory';
import mockData from '@/components/report-page/mock';
import ReportMonthly from '@/components/report-page/monthly/ReportMonthly';
import ReportMyself from '@/components/report-page/myself/ReportMyself';
import ReportProvider from '@/components/report-page/ReportProvider';

import { type CurrencyType } from '@/types/currency';

import { Icons } from '@/assets';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const ReportPage = () => {
  const [currencyType, setCurrencyType] = useState<CurrencyType>('BASE');

  const categoryData = mockData.compareByCategory;
  const myselfData = mockData.compareWithLastMonth;
  const monthlyData = mockData.compareWithAverage;

  const now = new Date();
  const [selectedDate, setSelectedDate] = useState(now);
  const startDate = useAccountBookStore(
    (state) => state.accountBook?.startDate,
  );
  const endDate = useAccountBookStore((state) => state.accountBook?.endDate);

  // startDate와 endDate에서 년월 추출
  const parseYearMonth = (dateString: string | undefined) => {
    if (!dateString) return null;
    const [year, month] = dateString.split('-').map(Number);
    return { year, month };
  };

  const startYearMonth = parseYearMonth(startDate);
  const endYearMonth = parseYearMonth(endDate);

  // 월 이동 가능 여부 체크 함수
  const canGoToMonth = (offset: number): boolean => {
    const targetDate = new Date(selectedDate);
    targetDate.setMonth(targetDate.getMonth() + offset);
    const targetYear = targetDate.getFullYear();
    const targetMonth = targetDate.getMonth() + 1;

    if (offset < 0) {
      if (!startYearMonth) return true;
      return (
        targetYear > startYearMonth.year ||
        (targetYear === startYearMonth.year &&
          targetMonth >= startYearMonth.month)
      );
    } else {
      if (!endYearMonth) return true;
      return (
        targetYear < endYearMonth.year ||
        (targetYear === endYearMonth.year && targetMonth <= endYearMonth.month)
      );
    }
  };

  const handleMonthChange = (offset: number) => {
    setSelectedDate((prev) => {
      const newDate = new Date(prev);
      newDate.setMonth(newDate.getMonth() + offset);
      return newDate;
    });
  };

  const canGoPrev = canGoToMonth(-1);
  const canGoNext = canGoToMonth(1);

  const getNavButtonClass = (enabled: boolean) =>
    clsx('size-6', {
      'text-label-alternative cursor-pointer': enabled,
      'text-label-disable': !enabled,
    });

  const year = selectedDate.getFullYear();
  const month = selectedDate.getMonth() + 1;

  return (
    <div className="flex min-w-283 flex-col gap-8 px-30 pt-8">
      <div className="flex flex-col gap-3">
        <span className="title2-bold text-label-normal">분석</span>
        <span className="headline1-medium text-label-alternative">
          내 월별 소비를 같은 국가 교환학생과 비교하고 전월 대비 현재 지출
          변화를 살펴봐요
        </span>
      </div>
      <div className="flex min-w-283 flex-col gap-4.5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-[21.5px]">
            <span className="title3-medium text-label-normal min-w-34">
              {year}년 {month}월
            </span>
            <div className="flex gap-3.5">
              <Icons.ChevronBack
                className={getNavButtonClass(canGoPrev)}
                onClick={() => canGoPrev && handleMonthChange(-1)}
              />
              <Icons.ChevronForward
                className={getNavButtonClass(canGoNext)}
                onClick={() => canGoNext && handleMonthChange(1)}
              />
            </div>
          </div>
          <div className="flex gap-3">
            <span className="body1-normal-medium text-label-alternative">
              현지통화로 보기
            </span>
            <Switch
              checked={currencyType === 'LOCAL'}
              onChange={(checked) =>
                setCurrencyType(checked ? 'LOCAL' : 'BASE')
              }
            />
          </div>
        </div>

        <div className="flex w-full min-w-283 gap-3.5">
          <ReportProvider
            currencyType={currencyType}
            onCurrencyTypeChange={setCurrencyType}
          >
            <div className="flex w-113 flex-col justify-between">
              <ReportMonthly data={monthlyData} />
              <ReportMyself data={myselfData} />
            </div>

            <div className="flex-1">
              <ReportCategory data={categoryData} />
            </div>
          </ReportProvider>
        </div>
      </div>
    </div>
  );
};

export default ReportPage;
