import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import type {
  CreateTravelRequest,
  GetImageUrlResponse,
  GetPresignedUrlRequest,
  GetPresignedUrlResponse,
  GetTravelAmountResponse,
  GetTravelDetailResponse,
  GetTravelsResponse,
  GetTravelWidgetLayoutResponse,
  GetTravelWidgetRequest,
  PatchTravelRequest,
  TravelWidgetResponseMap,
  UpdateTravelBudgetWidgetResponse,
  UpdateTravelRequest,
  UpdateTravelResponse,
  UpdateTravelWidgetLayoutRequest,
} from '@/api/travels/type';

export const getTravels = (
  accountBookId: number | string,
): Promise<GetTravelsResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.BASE(accountBookId),
    options: { method: 'GET' },
  });
};

export const createTravel = (
  accountBookId: number | string,
  data: CreateTravelRequest,
): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.BASE(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

export const getTravelDetail = (
  accountBookId: number | string,
  travelId: number | string,
): Promise<GetTravelDetailResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.DETAIL(accountBookId, travelId),
    options: { method: 'GET' },
  });
};

export const updateTravel = (
  accountBookId: number | string,
  travelId: number | string,
  data: UpdateTravelRequest,
): Promise<UpdateTravelResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.DETAIL(accountBookId, travelId),
    options: {
      method: 'PUT',
      body: JSON.stringify(data),
    },
  });
};

export const patchTravel = (
  accountBookId: number | string,
  travelId: number | string,
  data: PatchTravelRequest,
): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.DETAIL(accountBookId, travelId),
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

export const deleteTravel = (
  accountBookId: number | string,
  travelId: number | string,
): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.DETAIL(accountBookId, travelId),
    options: { method: 'DELETE' },
  });
};

export const getTravelPresignedUrl = (
  accountBookId: number | string,
  data: GetPresignedUrlRequest,
): Promise<GetPresignedUrlResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.PRESIGNED_URL(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

export const getTravelImageUrl = (
  accountBookId: number | string,
): Promise<GetImageUrlResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.IMAGE_URL(accountBookId),
    options: { method: 'GET' },
  });
};

export const getTravelAmount = (
  accountBookId: number | string,
  travelId: number | string,
): Promise<GetTravelAmountResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.AMOUNT(accountBookId, travelId),
    options: { method: 'GET' },
  });
};

export const getTravelWidgetLayout = (
  accountBookId: number | string,
  travelId: number | string,
): Promise<GetTravelWidgetLayoutResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.WIDGETS(accountBookId, travelId),
    options: { method: 'GET' },
  });
};

export const updateTravelWidgetLayout = (
  accountBookId: number | string,
  travelId: number | string,
  data: UpdateTravelWidgetLayoutRequest,
): Promise<GetTravelWidgetLayoutResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.WIDGETS(accountBookId, travelId),
    options: {
      method: 'PUT',
      body: JSON.stringify(data),
    },
  });
};

export const getTravelWidget = <T extends keyof TravelWidgetResponseMap>({
  accountBookId,
  travelId,
  widgetType,
  currencyType,
  period,
}: GetTravelWidgetRequest): Promise<TravelWidgetResponseMap[T]> => {
  return customFetch({
    endpoint: ENDPOINTS.WIDGETS.TRAVEL_DATA(accountBookId, travelId),
    params: {
      widgetType,
      ...(currencyType && { currencyType }),
      ...(period && { period }),
    },
    options: { method: 'GET' },
  });
};

export const updateTravelBudget = (
  accountBookId: number | string,
  travelId: number | string,
  budget: number,
): Promise<UpdateTravelBudgetWidgetResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TRAVELS.BUDGET(accountBookId, travelId),
    options: {
      method: 'PATCH',
      body: JSON.stringify({ budget }),
    },
  });
};
