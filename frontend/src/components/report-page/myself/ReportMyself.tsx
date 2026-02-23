import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import ReportLineGraph from '@/components/report-page/myself/ReportLineGraph';
import { useReportContext } from '@/components/report-page/ReportContext';

import { type AnalysisChartItem } from '@/api/account-books/type';
import { formatAmountByCountry, getCountryInfo } from '@/lib/country';
import { useAccountBookCountryCode } from '@/stores/accountBookStore';

interface ReportMyselfProps {
  isCurrentMonth: boolean;
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
  const countryCode = useAccountBookCountryCode(currencyType);
  const unit = getCountryInfo(countryCode)?.currencyUnitKor || '';

  const diffValue = Number(data.diff);
  const diff = Math.abs(diffValue);
  const isLess = diffValue < 0;
  const formattedDiff = formatAmountByCountry(diff, countryCode, 0);
  const formattedThisMonthSpent = formatAmountByCountry(
    Number(data.thisMonthSpent),
    countryCode,
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

  return (
    <ReportContainer title="전월 대비 지출 비교">
      <ReportContent className="h-fit w-109 pr-6">
        <div className="flex flex-col gap-2.5">
          <h3 className="heading1-bold text-label-normal">
            지난달보다{' '}
            <span className="text-primary-strong">
              {formattedDiff}
              {unit} {isLess ? '덜' : '더'}
            </span>{' '}
            쓰는 중이에요
          </h3>
          <span className="body1-normal-medium text-label-alternative">
            오늘까지 {formattedThisMonthSpent}
            {unit} 썼어요
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
