import { useAutoFitScale } from '@/hooks/useAutoFitScale';

import type { CategoryChartViewProps } from '@/components/chart/category/category.utils';
import CategoryLegendItem from '@/components/chart/category/CategoryLegendItem';
import PieChart from '@/components/chart/charts/PieChart';
import CurrencyAmountDisplay from '@/components/currency/CurrencyAmountDisplay';

const CategoryChartView = ({
  data,
  totalAmount,
  currencyType,
  countryCode,
}: CategoryChartViewProps) => {
  const MAX_WIDTH = 130; // w-32.5 (130px)
  const totalAmountNode = (
    <CurrencyAmountDisplay
      countryCode={countryCode}
      amount={totalAmount}
      size="lg"
    />
  );
  const { ref: contentRef, scale } = useAutoFitScale<HTMLDivElement>(
    MAX_WIDTH,
    [countryCode, totalAmount],
  );

  const pieChartData = data.map((item) => ({
    percentage: item.percentage,
    color: item.color,
  }));

  return (
    <>
      <PieChart data={pieChartData}>
        <div
          ref={contentRef}
          className="whitespace-nowrap"
          style={{
            transform: `scale(${scale})`,
            transformOrigin: 'center',
          }}
        >
          {totalAmountNode}
        </div>
      </PieChart>
      <div className="flex flex-col items-start justify-center gap-1">
        {data.map((item) => (
          <CategoryLegendItem
            key={item.categoryName}
            currencyType={currencyType}
            countryCode={countryCode}
            categoryName={item.categoryName}
            percentage={item.percentage}
            amount={item.amount}
            color={item.color}
          />
        ))}
      </div>
    </>
  );
};

export default CategoryChartView;
