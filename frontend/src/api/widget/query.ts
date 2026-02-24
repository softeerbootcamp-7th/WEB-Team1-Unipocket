import { useContext } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';

import type { WidgetType } from '@/components/chart/widget/type';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';

import type { CurrencyType } from '@/types/currency';
import type { PeriodType } from '@/types/period';

import { useTravelWidgetQuery } from '@/api/travels/query';
import {
  getWidget,
  getWidgetLayout,
  updateWidgetLayout,
} from '@/api/widget/api';
import { queryClient } from '@/main';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

import type { UpdateWidgetLayoutRequest, WidgetResponseMap } from './type';

export const widgetKeys = {
  all: ['widget'] as const,

  allDetails: (accountBookId: number | string | undefined) =>
    [...widgetKeys.all, 'detail', accountBookId] as const,

  detail: (
    accountBookId: number | undefined,
    widgetType: WidgetType,
    currencyType?: CurrencyType,
    period?: PeriodType,
  ) =>
    [
      ...widgetKeys.allDetails(accountBookId),
      widgetType,
      { currencyType, period },
    ] as const,

  layout: (accountBookId: number | undefined) =>
    [...widgetKeys.all, 'layout', accountBookId] as const,
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
  const accountBookId = useRequiredAccountBook().accountBookId;

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

export const useWidgetLayoutQuery = () => {
  const accountBookId = useRequiredAccountBook().accountBookId;

  return useQuery({
    queryKey: widgetKeys.layout(accountBookId),
    queryFn: () => getWidgetLayout(accountBookId),
    enabled: !!accountBookId,
  });
};

export const useUpdateWidgetLayoutMutation = () => {
  const accountBookId = useRequiredAccountBook().accountBookId;

  return useMutation({
    mutationFn: (data: UpdateWidgetLayoutRequest) =>
      updateWidgetLayout(accountBookId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: widgetKeys.layout(accountBookId),
      });
      toast.success('위젯 순서가 저장되었어요.');
    },
    onError: () => {
      toast.error('위젯 순서 저장에 실패했어요.');
    },
  });
};

/**
 * WidgetContext의 travelId 유무에 따라
 * 홈(useWidgetQuery) 또는 여행(useTravelWidgetQuery)을 자동으로 분기하는 훅
 */
export const useContextualWidgetQuery = <T extends keyof WidgetResponseMap>(
  widgetType: T,
  options: UseWidgetQueryOptions = {},
) => {
  const travelId = useContext(WidgetContext)?.travelId;
  const { enabled = true, ...rest } = options;

  const homeResult = useWidgetQuery(widgetType, {
    ...rest,
    enabled: !travelId && enabled,
  });

  const travelResult = useTravelWidgetQuery(travelId ?? '', widgetType, {
    ...rest,
    enabled: !!travelId && enabled,
  });

  return travelId ? travelResult : homeResult;
};
