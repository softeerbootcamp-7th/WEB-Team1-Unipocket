import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';

const ReportMonthly = () => {
  return (
    <ReportContainer title="월별 지출 비교">
      <ReportContent className="h-62 w-109 gap-7 py-8.75 pl-7">
        <h3 className="heading1-bold text-label-normal">
          나랑 같은 국가의 교환학생보다 <br />
          <span className="text-primary-strong">234달러 덜</span> 썼어요
        </h3>
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMonthly;
