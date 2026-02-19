import { useQuery } from '@tanstack/react-query';

import type { WidgetType } from '@/components/chart/widget/type';

import type { CurrencyType } from '@/types/currency';
import type { PeriodType } from '@/types/period';

import { getWidget } from '@/api/widget/api';
import type { WidgetResponse } from '@/api/widget/type';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

export const widgetKeys = {
  all: ['widget'] as const,
  detail: (
    accountBookId: string,
    widgetType: WidgetType,
    currencyType?: CurrencyType,
    period?: PeriodType,
  ) =>
    [
      'widget',
      accountBookId,
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
) => {
  const accountBookId = useAccountBookStore((state) => state.accountBook?.id);

  return useQuery({
    queryKey: widgetKeys.detail(
      String(accountBookId),
      widgetType,
      currencyType,
      period,
    ),
    queryFn: () =>
      getWidget<T>({
        accountBookId: String(accountBookId),
        widgetType,
        currencyType,
        period,
      }),
    enabled: enabled && !!accountBookId,
  });
};
