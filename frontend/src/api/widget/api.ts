import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import type {
  GetWidgetRequest,
  UpdateWidgetLayoutRequest,
  WidgetLayoutResponse,
} from '@/api/widget/type';

export const getWidget = <T>({
  accountBookId,
  widgetType,
  currencyType,
  period,
}: GetWidgetRequest): Promise<T> => {
  const params = new URLSearchParams({ widgetType });
  if (currencyType) params.set('currencyType', currencyType);
  if (period) params.set('period', period);

  return customFetch({
    endpoint: `${ENDPOINTS.WIDGETS.ACCOUNT_BOOK_DATA(accountBookId)}?${params.toString()}`,
    options: {
      method: 'GET',
    },
  });
};

export const getWidgetLayout = (
  accountBookId: number | string,
): Promise<WidgetLayoutResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.WIDGETS.ACCOUNT_BOOK_LAYOUT(accountBookId),
    options: {
      method: 'GET',
    },
  });
};

export const updateWidgetLayout = (
  accountBookId: number | string,
  data: UpdateWidgetLayoutRequest,
): Promise<WidgetLayoutResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.WIDGETS.ACCOUNT_BOOK_LAYOUT(accountBookId),
    options: {
      method: 'PUT',
      body: JSON.stringify(data),
    },
  });
};
