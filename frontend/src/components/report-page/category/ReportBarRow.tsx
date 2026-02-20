import ReportBar from '@/components/report-page/category/ReportBar';

import { CATEGORIES, type CategoryId } from '@/types/category';

interface ReportBarRowProps {
  categoryId: CategoryId;
  me: number;
  other: number;
  maxLabel: number;
}

const ReportBarRow = ({
  categoryId,
  me,
  other,
  maxLabel,
}: ReportBarRowProps) => {
  return (
    <div className="flex h-10 items-center gap-4">
      <span className="label1-normal-medium text-label-neutral flex w-12 justify-end">
        {CATEGORIES[categoryId].name}
      </span>

      <div className="flex w-full flex-col justify-between">
        <ReportBar value={me} variant="me" maxValue={maxLabel} />
        <ReportBar value={other} variant="other" maxValue={maxLabel} />
      </div>
    </div>
  );
};

export default ReportBarRow;
