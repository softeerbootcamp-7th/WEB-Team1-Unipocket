import type { PeriodData } from '@/components/chart/chartType';
import {
  generateDailyLabels,
  generateMonthlyLabels,
  generateWeeklyLabels,
} from '@/components/chart/period/period.utils';

// ─── 월별 mock (6개월) ──────────────────────────
const monthlyLabels = generateMonthlyLabels();
export const MOCK_MONTHLY_DATA: PeriodData[] = monthlyLabels.map(
  (label, i) => ({
    label,
    value: [0, 320000, 180000, 450000, 270000, 150000][i] ?? 0,
  }),
);

// ─── 주별 mock (5주) ────────────────────────────
const weeklyLabels = generateWeeklyLabels();
export const MOCK_WEEKLY_DATA: PeriodData[] = weeklyLabels.map((label, i) => ({
  label,
  value: [120000, 95000, 50000, 210000, 80000][i] ?? 0,
}));

// ─── 일별 mock (7일) ────────────────────────────
const dailyLabels = generateDailyLabels();
export const MOCK_DAILY_DATA: PeriodData[] = dailyLabels.map((label, i) => ({
  label,
  value: [60000, 30000, 80000, 50000, 70000, 90000, 20000][i] ?? 0,
}));
