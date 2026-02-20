import { CATEGORY_CHART_COLORS } from '@/components/chart/chartType';

import { CATEGORIES, type CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

import type { CategoryWidgetResponse } from '@/api/widget/type';
import type { CountryCode } from '@/data/country/countryCode';

interface CategoryChartItem {
  percentage: number;
  categoryName: CategoryType;
  amount: number;
  color: string;
}

export interface CategoryChartViewProps {
  data: CategoryChartItem[];
  totalAmount: number;
  currencyType: CurrencyType;
  countryCode: CountryCode;
}

export function transformCategoryChartData(
  data?: CategoryWidgetResponse,
): CategoryChartItem[] {
  if (!data) return [];

  return data.items
    .filter((item) => item.percent > 0)
    .map((item, idx) => ({
      percentage: item.percent,
      categoryName: CATEGORIES[item.category].name,
      amount: Number(item.amount),
      color: CATEGORY_CHART_COLORS[idx % CATEGORY_CHART_COLORS.length],
    }));
}
