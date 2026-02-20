import { useQuery } from '@tanstack/react-query';

import type { WidgetType } from '@/components/chart/widget/type';

import type { CurrencyType } from '@/types/currency';
import type { PeriodType } from '@/types/period';

import { getWidget } from '@/api/widget/api';
import { useRequiredAccountBook } from '@/stores/useAccountBookStore';

import type { WidgetResponseMap } from './type';

export const widgetKeys = {
  all: ['widget'] as const,

  detail: (
    accountBookId: number | undefined,
    widgetType: WidgetType,
    currencyType?: CurrencyType,
    period?: PeriodType,
  ) => ['widget', accountBookId, widgetType, { currencyType, period }] as const,
};

interface UseWidgetQueryOptions {
  currencyType?: CurrencyType;
  period?: PeriodType;
  enabled?: boolean;
}

export const useWidgetQuery = <T extends keyof WidgetResponseMap>(
  widgetType: T,
  { currencyType, period, enabled = true }: UseWidgetQueryOptions = {},
) => {
  const accountBookId = useRequiredAccountBook().id;

  return useQuery({
    queryKey: widgetKeys.detail(
      accountBookId,
      widgetType,
      currencyType,
      period,
    ),
    queryFn: () =>
      getWidget<WidgetResponseMap[T]>({
        accountBookId: accountBookId!,
        widgetType,
        currencyType,
        period,
      }),
    enabled: enabled && !!accountBookId,
  });
};
