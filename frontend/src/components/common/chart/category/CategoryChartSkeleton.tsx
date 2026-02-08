import PieChart from '../charts/PieChart';
import { mockData } from './mock';

const CategoryLegendItemSkeleton = () => {
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

const CategoryChartSkeleton = () => {
  const CATEGORY_COUNT = 7;
  return (
    <>
      <div>
        <PieChart
          data={mockData.items.map((item) => ({
            percentage: item.percent,
            color: 'var(--color-fill-strong)',
          }))}
        >
          <div className="bg-fill-normal rounded-modal-4 h-5.5 w-32.5 animate-pulse" />
        </PieChart>
      </div>
      <div className="flex flex-col justify-between">
        {Array.from({ length: CATEGORY_COUNT }).map((_, idx) => (
          <CategoryLegendItemSkeleton key={idx} />
        ))}
      </div>
    </>
  );
};

export default CategoryChartSkeleton;
