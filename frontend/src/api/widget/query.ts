import { useQuery } from '@tanstack/react-query';

import type { PeriodType, WidgetType } from '@/components/chart/widget/type';

import type { CurrencyType } from '@/types/currency';

import { getWidget } from '@/api/widget/api';
import type { WidgetResponse } from '@/api/widget/type';

const ACCOUNT_BOOK_ID = '900019';

export const widgetKeys = {
  all: ['widget'] as const,
  detail: (
    widgetType: WidgetType,
    currencyType?: CurrencyType,
    period?: PeriodType,
  ) =>
    [
      'widget',
      ACCOUNT_BOOK_ID,
      widgetType,
      ...(currencyType ? [currencyType] : []),
      ...(period ? [period] : []),
    ] as const,
};

interface UseWidgetQueryOptions {
  currencyType?: CurrencyType;
  period?: PeriodType;
  enabled?: boolean;
}

export const useWidgetQuery = <T extends WidgetResponse>(
  widgetType: WidgetType,
  { currencyType, period, enabled = true }: UseWidgetQueryOptions = {},
) =>
  useQuery({
    queryKey: widgetKeys.detail(widgetType, currencyType, period),
    queryFn: () =>
      getWidget<T>({
        accountBookId: ACCOUNT_BOOK_ID,
        widgetType,
        currencyType,
        period,
      }),
    enabled,
  });
