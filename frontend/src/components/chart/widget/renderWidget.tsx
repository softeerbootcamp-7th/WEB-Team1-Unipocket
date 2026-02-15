import BudgetChart from '@/components/chart/budget/BudgetChart';
import CategoryChart from '@/components/chart/category/CategoryChart';
import type { ChartMode } from '@/components/chart/chartType';
import ComparisonChart from '@/components/chart/comparison/ComparisonChart';
import ExpenseChart from '@/components/chart/expense/ExpenseChart';
import PeriodChart from '@/components/chart/period/PeriodChart';
import type { WidgetItem } from '@/components/chart/widget/type';
import type { WidgetKind } from '@/components/chart/widget/type';

const widgetComponentMap: Record<WidgetKind, React.ComponentType<ChartMode>> = {
  BUDGET: BudgetChart,
  PERIOD: PeriodChart,
  CATEGORY: CategoryChart,
  COMPARISON: ComparisonChart,
  PAYMENT: (props) => <ExpenseChart {...props} mode="method" />,
  CURRENCY: (props) => <ExpenseChart {...props} mode="currency" />,
  BLANK: () => (
    <div className="rounded-modal-16 border-line-solid-normal h-72 w-67 border" />
  ),
};

export const renderWidget = (widget: WidgetItem, options: ChartMode = {}) => {
  const Component = widgetComponentMap[widget.widgetType];

  if (!Component) {
    return null;
  }

  return <Component {...options} />;
};
