import ReportBarGraph from '@/components/report-page/category/ReportBarGraph';
import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';

import { type CategoryId,getCategoryName } from '@/types/category';

interface ReportCategoryProps {
  data: {
    maxDiffCategoryIndex: CategoryId;
    isOverSpent: boolean;
    maxLabel: string;
    items: {
      categoryIndex: CategoryId;
      mySpentAmount: string;
      averageSpentAmount: string;
    }[];
  };
}

const ReportCategory = ({ data }: ReportCategoryProps) => {
  const category = getCategoryName(data.maxDiffCategoryIndex);
  const maxLabelValue = Number(data.maxLabel);

  return (
    <ReportContainer title="월별 지출 비교">
      <ReportContent className="h-166.5 w-162.5 gap-7">
        <h3 className="heading1-bold text-label-normal">
          나랑 같은 국가의 교환학생보다{' '}
          <span className="text-primary-normal">{category}</span> 소비가 유독{' '}
          {data.isOverSpent ? '많아요' : '적어요'}
        </h3>
        <ReportBarGraph maxLabel={maxLabelValue} items={data.items} />
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportCategory;
