export const PERIOD_TYPES = ['ALL', 'MONTHLY', 'WEEKLY', 'DAILY'] as const;
export type PeriodType = (typeof PERIOD_TYPES)[number];

export const PERIOD_ID: Record<PeriodType, number> = {
  ALL: 0,
  MONTHLY: 1,
  WEEKLY: 2,
  DAILY: 3,
} as const;

export const PERIOD_LABEL: Record<PeriodType, string> = {
  ALL: '전체',
  MONTHLY: '월별',
  WEEKLY: '주별',
  DAILY: '일별',
} as const;

export interface PeriodOption {
  id: number; // Dropdown용
  name: string; // UI용(한글)
  type: PeriodType; // API용(영문)
}

export const PERIOD_OPTIONS: PeriodOption[] = PERIOD_TYPES.map((type) => ({
  id: PERIOD_ID[type],
  name: PERIOD_LABEL[type],
  type,
}));

export const getPeriodOptionById = (id: number): PeriodOption => {
  return PERIOD_OPTIONS[id] ?? PERIOD_OPTIONS[0];
};

export const getPeriodTypeById = (id: number): PeriodType => {
  return getPeriodOptionById(id).type;
};
