// 해당 타입은 API 연동 시 경로 이동 예정
export type BudgetStatisticsResponse = {
  totalBudget: number;
  usedBudget: number;
};

export const mockData: BudgetStatisticsResponse = {
  totalBudget: 10000000,
  usedBudget: 6502000,
};
