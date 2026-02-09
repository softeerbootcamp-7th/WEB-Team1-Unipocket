import ReportContainer from '../layout/ReportContainer';
import ReportContent from '../layout/ReportContent';
import ReportBarGraph from './ReportBarGraph';

const category = '생활';

const ReportCategory = () => {
  return (
    <ReportContainer title="월별 지출 비교" className="h-182 w-166.5">
      <ReportContent className="h-166.5 gap-7">
        <h3 className="heading1-bold text-label-normal">
          나랑 같은 국가의 교환학생보다{' '}
          <span className="text-primary-normal">{category}</span> 소비가 유독
          많아요
        </h3>
        <ReportBarGraph />
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportCategory;
