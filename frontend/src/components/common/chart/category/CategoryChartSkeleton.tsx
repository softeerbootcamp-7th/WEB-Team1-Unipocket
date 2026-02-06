import CategoryPieChart from './CategoryPieChart';
import { mockData } from './mock';

export const CategoryPieChartSkeleton = () => {
  return (
    <div>
      <CategoryPieChart
        data={mockData.items.map((item) => ({
          categoryName: item.categoryName,
          percentage: item.percent,
        }))}
        totalAmount={
          <div className="bg-fill-normal rounded-modal-4 h-5.5 w-32.5 animate-pulse" />
        }
        colors={['var(--color-fill-strong)']}
      />
    </div>
  );
};

export const CategoryListItemSkeleton = () => {
  return (
    <div className="flex h-6 w-52 items-center gap-2.5">
      <div className="bg-fill-strong size-3.5 animate-pulse" />
      <div className="flex w-full animate-pulse items-center justify-between">
        <div className="flex items-center gap-1.5">
          <div className="bg-fill-strong rounded-modal-4 h-3 w-5" />
          <div className="bg-fill-normal rounded-modal-4 h-3 w-6.5" />
        </div>
        <div className="bg-fill-normal rounded-modal-4 h-3 w-25" />
      </div>
    </div>
  );
};

const StatSectionSkeleton = () => {
  return (
    <>
      <CategoryPieChartSkeleton />
      <div className="flex flex-col justify-between">
        {Array.from({ length: 7 }).map((_, idx) => (
          <CategoryListItemSkeleton key={idx} />
        ))}
      </div>
    </>
  );
};

export default StatSectionSkeleton;
