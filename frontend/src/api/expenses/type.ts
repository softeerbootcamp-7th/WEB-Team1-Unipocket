// TODO: 백엔드에서 CategoryType 아직 인덱스로 안 주고 있어서\
// 추후 인덱스로 조면 src/types/category.ts 타입 사용 예정
export type CategoryType =
  | 'LEISURE'
  | 'FOOD'
  | 'TRANSPORT'
  | 'LODGING'
  | 'SHOPPING'
  | 'ETC';
export type PaymentMethodType = 'CARD' | 'CASH';
export type ExpenseSourceType = 'MANUAL' | 'OCR'; // 수동 입력 또는 OCR 등

export interface ExpenseResponse {
  expenseId: number;
  accountBookId: number;
  travelId: number | null;
  merchantName: string;
  displayMerchantName: string;
  category: CategoryType;
  paymentMethod: PaymentMethodType;
  occurredAt: string; // "2026-02-08T21:53:19"
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

/** * 지출 생성 요청 타입
 * (보통 생성 시에는 ID나 계산된 기준 통화 금액을 제외하고 보냅니다)
 */
export interface CreateExpenseRequest {
  merchantName: string;
  category: CategoryType;
  paymentMethod: PaymentMethodType;
  occurredAt: string;
  localCurrencyAmount: number;
  localCurrencyCode: string;
  memo?: string;
}

/** 목록 조회 응답 (일반적으로 배열 형태이거나 페이징 객체에 담겨 옵니다) */
export type GetExpensesResponse = ExpenseResponse[];
