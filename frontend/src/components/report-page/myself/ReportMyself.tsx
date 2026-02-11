import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';

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
  return (
    <ReportContainer title="전월 대비 지출 비교">
      <ReportContent className="h-166.5 w-162.5 gap-7">
        <h3 className="heading1-bold text-label-normal">
          지난달 이맘때보다{' '}
          <span className="text-primary-normal">{data.diff}만원 더</span> 쓰는 중이에요
        </h3>
      </ReportContent>
    </ReportContainer>
  );
}

export default ReportMyself