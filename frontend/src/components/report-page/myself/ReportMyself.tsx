import { useMemo } from 'react';

import { useAutoFitScale } from '@/hooks/useAutoFitScale';

import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import ReportLineGraph from '@/components/report-page/myself/ReportLineGraph';
import { useReportContext } from '@/components/report-page/ReportContext';

import { type AnalysisChartItem } from '@/api/account-books/type';
import { formatAmountByCountry, getCountryInfo } from '@/lib/country';
import { useAccountBookCountryCode } from '@/stores/accountBookStore';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface ReportMyselfProps {
  isCurrentMonth: boolean;
  isPlaceholderData: boolean;
  data: {
    diff: string;
    thisMonth: string;
    thisMonthCount: number;
    lastMonth: string;
    lastMonthCount: number;
    totalSpent: {
      thisMonthToDate: string;
      lastMonthTotal: string;
    };
    thisMonthSpent: string;
    thisMonthItem: AnalysisChartItem[];
    prevMonthItem: AnalysisChartItem[];
  };
}

const ReportMyself = ({ data, isCurrentMonth }: ReportMyselfProps) => {
  const { currencyType } = useReportContext();
  const { baseCountryCode } = useRequiredAccountBook();
  const countryCode = useAccountBookCountryCode(currencyType);
  // 단위는 currencyType이 바뀔 때만 변경
  const displayCountryCode = useMemo(
    () => (currencyType === 'LOCAL' ? countryCode : baseCountryCode),
    [currencyType, countryCode, baseCountryCode],
  );
  const unit = useMemo(
    () => getCountryInfo(displayCountryCode)?.currencyUnitKor || '',
    [displayCountryCode],
  );

  // 숫자만 placeholderData 여부에 따라 바뀜
  const diffValue = Number(data.diff);
  const diff = Math.abs(diffValue);
  const isLess = diffValue < 0;
  const formattedDiff = formatAmountByCountry(diff, displayCountryCode, 0);
  const formattedThisMonthSpent = formatAmountByCountry(
    Number(data.thisMonthSpent),
    displayCountryCode,
    0,
  );

  const thisMonthData = {
    label: data.thisMonth,
    dayCount: data.thisMonthCount,
    totalSpent: data.totalSpent.thisMonthToDate,
    items: data.thisMonthItem,
  };

  const lastMonthData = {
    label: data.lastMonth,
    dayCount: data.lastMonthCount,
    totalSpent: data.totalSpent.lastMonthTotal,
    items: data.prevMonthItem,
  };

  const maxValue = Math.max(
    Number(lastMonthData.totalSpent),
    Number(thisMonthData.totalSpent),
  );

  const { ref: headerRef, scale: headerScale } =
    useAutoFitScale<HTMLHeadingElement>(384, [formattedDiff, unit, isLess]);

  return (
    <ReportContainer title="전월 대비 지출 비교">
      <ReportContent className="h-fit w-109 pr-6">
        <div className="flex flex-col gap-2.5">
          <h3
            ref={headerRef}
            className="heading1-bold text-label-normal whitespace-nowrap"
            style={{
              transform: `scale(${headerScale})`,
              transformOrigin: 'left',
            }}
          >
            지난달보다{' '}
            <span className="text-primary-strong">
              {`${formattedDiff}${unit} ${isLess ? '덜' : '더'}`}
            </span>{' '}
            쓰는 중이에요
          </h3>
          <span className="body1-normal-medium text-label-alternative">
            오늘까지 {`${formattedThisMonthSpent}${unit}`} 썼어요
          </span>
        </div>
        <ReportLineGraph
          thisMonth={thisMonthData}
          lastMonth={lastMonthData}
          maxValue={maxValue}
          isCurrentMonth={isCurrentMonth}
        />
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMyself;
