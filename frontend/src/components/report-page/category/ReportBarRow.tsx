import { type CategoryId,getCategoryName } from '@/types/category';

import ReportBar from './ReportBar';

interface ReportBarRowProps {
  categoryIndex: CategoryId;
  me: number;
  other: number;
  maxLabel: number;
}

const ReportBarRow = ({
  categoryIndex,
  me,
  other,
  maxLabel,
}: ReportBarRowProps) => {
  return (
    <div className="flex h-10 items-center gap-4">
      <span className="label1-normal-medium text-label-neutral flex w-12 justify-end">
        {getCategoryName(categoryIndex)}
      </span>

      <div className="flex w-full flex-col justify-between">
        <ReportBar value={me} variant="me" maxValue={maxLabel} />
        <ReportBar value={other} variant="other" maxValue={maxLabel} />
      </div>
    </div>
  );
};

export default ReportBarRow;
