import { useState } from 'react';

import Switch from '@/components/common/Switch';
import ReportCategory from '@/components/report-page/category/ReportCategory';
import ReportMonthly from '@/components/report-page/monthly/ReportMonthly';
import ReportMyself from '@/components/report-page/myself/ReportMyself';
import ReportProvider from '@/components/report-page/ReportProvider';

import {
  canGoToMonth,
  dateToYearMonth,
  getMaxYearMonth,
  getNavButtonClass,
  parseYearMonth,
} from '@/pages/report-page/report.utils';

import { type CurrencyType } from '@/types/currency';

import { useAnalysisQuery } from '@/api/account-books/query';
import { Icons } from '@/assets';
import { useRequiredAccountBook } from '@/stores/accountBook';

const ReportPage = () => {
  const [currencyType, setCurrencyType] = useState<CurrencyType>('BASE');

  const accountBookId = useRequiredAccountBook().id;

  const now = new Date();
  const [selectedDate, setSelectedDate] = useState(now);
  const startDate = useRequiredAccountBook().startDate;
  const endDate = useRequiredAccountBook().endDate;

  const startYearMonth = parseYearMonth(startDate);
  const endYearMonth = parseYearMonth(endDate);

  const nowYearMonth = dateToYearMonth(now);
  const maxYearMonth = getMaxYearMonth(endYearMonth, nowYearMonth);

  const handleMonthChange = (offset: number) => {
    setSelectedDate((prev) => {
      const newDate = new Date(prev);
      newDate.setMonth(newDate.getMonth() + offset);
      return newDate;
    });
  };

  const canGoPrev = canGoToMonth(
    selectedDate,
    -1,
    startYearMonth,
    maxYearMonth,
  );
  const canGoNext = canGoToMonth(selectedDate, 1, startYearMonth, maxYearMonth);

  const year = selectedDate.getFullYear();
  const month = selectedDate.getMonth() + 1;

  const { data } = useAnalysisQuery(accountBookId, year, month, currencyType);

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

        {/* @TODO: data 없을 때 보여줄 화면 추가하기 */}
        {data && (
          <div className="flex w-full min-w-283 gap-3.5">
            <ReportProvider
              currencyType={currencyType}
              onCurrencyTypeChange={setCurrencyType}
            >
              <div className="flex w-113 flex-col justify-between">
                <ReportMonthly data={data.compareWithAverage} />
                <ReportMyself data={data.compareWithLastMonth} />
              </div>

              <div className="flex-1">
                <ReportCategory data={data.compareByCategory} />
              </div>
            </ReportProvider>
          </div>
        )}
      </div>
    </div>
  );
};

export default ReportPage;
