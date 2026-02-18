import type { CategoryType } from '@/types/category';

// 지출 소스 타입
export type ExpenseSourceType =
  | 'MANUAL'
  | 'IMAGE_RECEIPT'
  | 'IMAGE_APP_CAPTURE'
  | 'CSV';

// 카드사 타입
export type CardCompanyType = string; // 백엔드 CardCompany enum에 맞춰 필요시 구체화

// 결제 수단 응답
export interface PaymentMethodResponse {
  isCash: boolean;
  card: {
    company: CardCompanyType;
    label: string;
    lastDigits: string;
  } | null;
}

// 지출 내역 상세 응답
export interface ExpenseResponse {
  expenseId: number;
  accountBookId: number;
  travelId: number | null;
  merchantName: string;
  displayMerchantName: string;
  category: CategoryType;
  paymentMethod: PaymentMethodResponse;
  occurredAt: string; // ISO-8601 format
  localCurrencyAmount: number;
  localCurrencyCode: string;
  baseCurrencyAmount: number;
  baseCurrencyCode: string;
  memo: string | null;
  source: ExpenseSourceType;
  approvalNumber: string | null;
  cardNumber: string | null;
  fileLink: string | null;
}

// 지출 내역 목록 조회 응답
export interface GetExpensesResponse {
  expenses: ExpenseResponse[];
  totalCount: number;
  page: number;
  size: number;
}

// 지출 생성/수정 요청
export interface CreateExpenseRequest {
  merchantName: string;
  category: CategoryType;
  userCardId?: number | null; // 카드 결제 시 카드 ID (null이면 현금)
  occurredAt: string; // ISO-8601 format
  localCurrencyAmount: number;
  localCurrencyCode: string;
  memo?: string | null;
  travelId?: number | null;
}

// 지출 검색 필터
export interface ExpenseSearchFilter {
  startDate?: string; // ISO-8601 format
  endDate?: string;
  category?: number;
  minAmount?: number;
  maxAmount?: number;
  merchantName?: string;
  travelId?: number;
  page?: number;
  size?: number;
  sort?: string; // 예: "occurredAt,desc"
}

// 거래처명 검색 응답
export interface MerchantNamesResponse {
  merchantNames: string[];
}
