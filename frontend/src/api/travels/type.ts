import type { WidgetType } from '@/components/chart/widget/type';

import type { CurrencyType } from '@/types/currency';
import type { PeriodType } from '@/types/period';

import type { WidgetLayoutItem, WidgetResponseMap } from '@/api/widget/type';

export type { WidgetResponseMap as TravelWidgetResponseMap };

export interface TravelWidget {
  type: string; // e.g. 'BUDGET'
  order: number;
}

export interface TravelBase {
  travelId: number;
  accountBookId: number;
  travelPlaceName: string;
  startDate: string; // YYYY-MM-DD
  endDate: string; // YYYY-MM-DD
  imageKey: string;
}

// GET /account-books/{accountBookId}/travels/{travelId}
export interface GetTravelDetailResponse extends TravelBase {
  widgets: TravelWidget[];
}

// PUT /account-books/{accountBookId}/travels/{travelId}
export interface UpdateTravelRequest {
  travelPlaceName: string;
  startDate: string;
  endDate: string;
  imageKey: string;
}

export interface UpdateTravelResponse {
  travelPlaceName: string;
  startDate: string;
  endDate: string;
  imageKey: string;
}

// PATCH /account-books/{accountBookId}/travels/{travelId}
export type PatchTravelRequest = UpdateTravelRequest;

// GET /account-books/{accountBookId}/travels
export type GetTravelsResponse = TravelBase[];

// POST /account-books/{accountBookId}/travels
export type CreateTravelRequest = UpdateTravelRequest;

// POST /account-books/{accountBookId}/travels/images/presigned-url
export interface GetPresignedUrlRequest {
  mimeType: string;
}

export interface GetPresignedUrlResponse {
  presignedUrl: string;
  imageKey: string;
  expiresIn: number;
}

// GET /account-books/{accountBookId}/travels/image-url
export interface GetImageUrlResponse {
  imageKey: string;
  presignedUrl: string;
  expiresIn: number;
}

// GET /account-books/{accountBookId}/travels/{travelId}/amount
export interface GetTravelAmountResponse {
  localCountryCode: string;
  localCurrencyCode: string;
  baseCountryCode: string;
  baseCurrencyCode: string;
  totalLocalAmount: number;
  totalBaseAmount: number;
}

// GET|PUT /account-books/{accountBookId}/travels/{travelId}/widgets
export type TravelWidgetLayoutItem = WidgetLayoutItem;
export type GetTravelWidgetLayoutResponse = WidgetLayoutItem[];
export type UpdateTravelWidgetLayoutRequest = WidgetLayoutItem[];

// GET /account-books/{accountBookId}/travels/{travelId}/widget
export interface GetTravelWidgetRequest {
  accountBookId: number | string;
  travelId: number | string;
  widgetType: WidgetType;
  currencyType?: CurrencyType;
  period?: PeriodType;
}

export interface UpdateTravelBudgetWidgetResponse {
  travelId: number;
  budget: number;
  budgetCreatedAt: string;
}
