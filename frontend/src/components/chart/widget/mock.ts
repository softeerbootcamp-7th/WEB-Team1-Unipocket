// 해당 파일은 API 연동 시 삭제 예정

import type { WidgetItem } from '@/components/chart/widget/type';

export const MOCK_WIDGET_DATA: WidgetItem[] = [
  {
    order: 0,
    widgetType: 'BUDGET',
    currencyType: 'BASE',
    period: 'MONTHLY',
  },
  {
    order: 1,
    widgetType: 'CATEGORY',
    currencyType: 'BASE',
    period: 'MONTHLY',
  },
  {
    order: 2,
    widgetType: 'PAYMENT',
    currencyType: 'LOCAL',
    period: 'ALL',
  },
];
