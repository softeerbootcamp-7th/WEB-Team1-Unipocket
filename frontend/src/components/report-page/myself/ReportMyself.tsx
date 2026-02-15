import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import ReportLineGraph from '@/components/report-page/myself/ReportLineGraph';
import { useReportContext } from '@/components/report-page/ReportContext';
import { type ChartItem } from '@/components/report-page/reportType';

import { type CountryCode } from '@/data/countryCode';
import { getCountryInfo } from '@/lib/country';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

type MonthlyData = {
  label: string; // "1월"
  dayCount: number; // 20
  totalSpent: string; // 이번달은 toDate, 지난달은 전체
  items: ChartItem[];
};

interface ReportMyselfProps {
  data: {
    diff: string;
    thisMonth: MonthlyData;
    lastMonth: MonthlyData;
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

  const maxValue = Math.max(
    Number(data.lastMonth.totalSpent),
    Number(data.thisMonth.totalSpent),
  );

  return (
    <ReportContainer title="전월 대비 지출 비교">
      <ReportContent className="h-86.25 w-109 pb-5 pl-6.5">
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
            오늘까지 {data.thisMonth.totalSpent}
            {unit} 썼어요
          </span>
        </div>
        <ReportLineGraph
          thisMonth={data.thisMonth}
          lastMonth={data.lastMonth}
          maxValue={maxValue}
        />
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMyself;
