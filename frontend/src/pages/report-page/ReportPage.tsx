import { useState } from 'react';

import Switch from '@/components/common/Switch';
import PageHeader from '@/components/layout/PageHeader';
import ReportControlSection from '@/components/report-page/ReportControlSection';
import ReportSection from '@/components/report-page/ReportSection';

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
import { PAGE_TITLE } from '@/constants/message';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const ReportPage = () => {
  const [currencyType, setCurrencyType] = useState<CurrencyType>('BASE');

  const accountBookId = useRequiredAccountBook().accountBookId;

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
  const isCurrentMonth =
    year === now.getFullYear() && month === now.getMonth() + 1;

  const { data } = useAnalysisQuery(accountBookId, year, month, currencyType);

  return (
    <div className="flex min-w-283 flex-col gap-8 px-30 pt-8">
      <PageHeader {...PAGE_TITLE.REPORT} />
      <div className="flex flex-col gap-4.5">
        <ReportControlSection
          year={year}
          month={month}
          canGoPrev={canGoPrev}
          canGoNext={canGoNext}
          onMonthChange={handleMonthChange}
          currencyType={currencyType}
          onCurrencyChange={(checked) =>
            setCurrencyType(checked ? 'LOCAL' : 'BASE')
          }
        />

        {/* @TODO: data 없을 때 보여줄 화면 추가하기 */}
        {data && (
          <ReportSection
            key={`${year}-${month}`}
            data={data}
            currencyType={currencyType}
            onCurrencyTypeChange={setCurrencyType}
            isCurrentMonth={isCurrentMonth}
          />
        )}
      </div>
    </div>
  );
};

export default ReportPage;
