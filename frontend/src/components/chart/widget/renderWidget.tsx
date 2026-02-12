import BudgetChart from '@/components/chart/budget/BudgetChart';
import CategoryChart from '@/components/chart/category/CategoryChart';
import type { ChartMode } from '@/components/chart/chartType';
import ComparisonChart from '@/components/chart/comparison/ComparisonChart';
import ExpenseChart from '@/components/chart/expense/ExpenseChart';
import PeriodChart from '@/components/chart/period/PeriodChart';
import type { WidgetItem } from '@/components/chart/widget/type';

export const renderWidget = (widget: WidgetItem, options: ChartMode = {}) => {
  const { isPreview = false } = options;

  switch (widget.widgetType) {
    case 'BUDGET':
      return <BudgetChart isPreview={isPreview} />;

    case 'PERIOD':
      return <PeriodChart isPreview={isPreview} />;

    case 'CATEGORY':
      return <CategoryChart isPreview={isPreview} />;

    case 'COMPARISON':
      return <ComparisonChart isPreview={isPreview} />;

    case 'PAYMENT':
      return <ExpenseChart mode="method" isPreview={isPreview} />;

    case 'CURRENCY':
      return <ExpenseChart mode="currency" isPreview={isPreview} />;

    case 'BLANK':
      return (
        <div className="rounded-modal-16 border-line-solid-normal h-72 w-67 border" />
      );

    default:
      return null;
  }
};
