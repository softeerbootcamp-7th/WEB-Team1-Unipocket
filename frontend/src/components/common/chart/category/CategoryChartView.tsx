import { useAutoFitScale } from '@/hooks/useAutoFitScale';

import type { CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

import type { CountryCode } from '@/data/countryCode';

import CurrencyAmountDisplay from '../../currency/CurrencyAmountDisplay';
import PieChart from '../charts/PieChart';
import { CATEGORY_CHART_COLORS } from '../chartType';
import CategoryLegendItem from './CategoryLegendItem';

interface CategoryChartViewProps {
  data: {
    percentage: number;
    categoryName: CategoryType;
    amount: number;
  }[];
  totalAmount: number;
  colors?: string[];
  currencyType: CurrencyType;
  countryCode: CountryCode;
}

const CategoryChartView = ({
  data,
  totalAmount,
  colors = CATEGORY_CHART_COLORS,
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

  const pieChartData = data.map((item, index) => ({
    percentage: item.percentage,
    color: colors[index % colors.length],
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
        {data.map((item, idx) => (
          <CategoryLegendItem
            key={item.categoryName}
            currencyType={currencyType}
            countryCode={countryCode}
            categoryName={item.categoryName}
            percentage={item.percentage}
            amount={item.amount}
            color={colors[idx % colors.length]}
          />
        ))}
      </div>
    </>
  );
};

export default CategoryChartView;
