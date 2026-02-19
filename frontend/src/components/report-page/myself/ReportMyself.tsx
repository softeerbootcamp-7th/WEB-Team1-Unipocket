import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import ReportLineGraph from '@/components/report-page/myself/ReportLineGraph';
import { useReportContext } from '@/components/report-page/ReportContext';
import { type ChartItem } from '@/components/report-page/reportType';

import { type CountryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

interface ReportMyselfProps {
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
    thisMonthItem: ChartItem[];
    prevMonthItem: ChartItem[];
  };
}

const ReportMyself = ({ data }: ReportMyselfProps) => {
  const { currencyType } = useReportContext();
  const countryCode = useAccountBookStore((state) =>
    currencyType === 'LOCAL'
      ? state.accountBook?.localCountryCode
      : state.accountBook?.baseCountryCode,
  ) as CountryCode;

  const unit = getCountryInfo(countryCode)?.currencyUnitKor || '';

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
      <ReportContent className="h-84.25 w-109 pr-6">
        <div className="flex flex-col gap-2.5">
          <h3 className="heading1-bold text-label-normal">
            지난달보다{' '}
            <span className="text-primary-strong">
              {data.diff}
              {unit} 더
            </span>{' '}
            쓰는 중이에요
          </h3>
          <span className="body1-normal-medium text-label-alternative">
            오늘까지 {thisMonthData.totalSpent}
            {unit} 썼어요
          </span>
        </div>
        <ReportLineGraph
          thisMonth={thisMonthData}
          lastMonth={lastMonthData}
          maxValue={maxValue}
        />
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMyself;
