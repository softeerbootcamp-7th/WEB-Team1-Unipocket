import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import ReportLineGraph from '@/components/report-page/myself/ReportLineGraph';
import { useReportContext } from '@/components/report-page/ReportContext';

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
      lastMonthToSameDay: string;
      lastMonthTotal: string;
    };
    thisMonthItem: { date: string; cumulatedAmount: string }[];
    prevMonthItem: { date: string; cumulatedAmount: string }[];
  };
}

const ReportMyself = ({ data }: ReportMyselfProps) => {
  const { currencyType } = useReportContext();
  const localCountryCode = useAccountBookStore(
    (state) => state.accountBook?.localCountryCode,
  ) as CountryCode;
  const baseCountryCode = useAccountBookStore(
    (state) => state.accountBook?.baseCountryCode,
  ) as CountryCode;

  const unit =
    currencyType === 'LOCAL'
      ? getCountryInfo(localCountryCode)?.currencyUnitKor || ''
      : getCountryInfo(baseCountryCode)?.currencyUnitKor || '';

  const maxValue = Math.max(
    Number(data.totalSpent.lastMonthTotal),
    Number(data.totalSpent.thisMonthToDate),
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
            오늘까지 {data.totalSpent.thisMonthToDate}
            {unit} 썼어요
          </span>
        </div>
        <ReportLineGraph
          thisMonthCount={data.thisMonthCount}
          lastMonthCount={data.lastMonthCount}
          maxValue={maxValue}
          thisMonthItem={data.thisMonthItem}
          prevMonthItem={data.prevMonthItem}
        />
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMyself;
