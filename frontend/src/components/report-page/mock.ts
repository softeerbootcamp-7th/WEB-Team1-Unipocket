import { type CategoryId } from '@/types/category';

interface CategoryItem {
  categoryIndex: CategoryId;
  mySpentAmount: string;
  averageSpentAmount: string;
}

interface MyselfItem {
  date: string;
  cumulatedAmount: string;
}

const mockData = {
  countryCode: 'US',
  compareWithAverage: {
    month: 12,
    mySpentAmount: '2456.02',
    averageSpentAmount: '2856.78',
    spentAmountDiff: '-400.76',
  },
  compareWithLastMonth: {
    diff: '150',
    thisMonth: '2026-01',
    thisMonthCount: 20,
    lastMonth: '2025-12',
    lastMonthCount: 31,
    totalSpent: {
      thisMonthToDate: '830',
      lastMonthToSameDay: '680',
      lastMonthTotal: '2311.46',
    },
    thisMonthItem: [
      { date: '2026-01-01', cumulatedAmount: '12.5' },
      { date: '2026-01-02', cumulatedAmount: '35.2' },
      { date: '2026-01-03', cumulatedAmount: '52.8' },
      { date: '2026-01-04', cumulatedAmount: '78.1' },
      { date: '2026-01-05', cumulatedAmount: '101' },
      { date: '2026-01-06', cumulatedAmount: '128.4' },
      { date: '2026-01-07', cumulatedAmount: '160.9' },
      { date: '2026-01-08', cumulatedAmount: '190.3' },
      { date: '2026-01-09', cumulatedAmount: '230.7' },
      { date: '2026-01-10', cumulatedAmount: '260.1' },
      { date: '2026-01-11', cumulatedAmount: '300.6' },
      { date: '2026-01-12', cumulatedAmount: '338.9' },
      { date: '2026-01-13', cumulatedAmount: '380.2' },
      { date: '2026-01-14', cumulatedAmount: '420.1' },
      { date: '2026-01-15', cumulatedAmount: '465' },
      { date: '2026-01-16', cumulatedAmount: '520.4' },
      { date: '2026-01-17', cumulatedAmount: '600.9' },
      { date: '2026-01-18', cumulatedAmount: '700.3' },
      { date: '2026-01-19', cumulatedAmount: '780.1' },
      { date: '2026-01-20', cumulatedAmount: '830' },
    ] as Array<MyselfItem>,
    prevMonthItem: [
      { date: '2025-12-01', cumulatedAmount: '18' },
      { date: '2025-12-02', cumulatedAmount: '26' },
      { date: '2025-12-03', cumulatedAmount: '41' },
      { date: '2025-12-04', cumulatedAmount: '58' },
      { date: '2025-12-05', cumulatedAmount: '82' },
      { date: '2025-12-06', cumulatedAmount: '103' },
      { date: '2025-12-07', cumulatedAmount: '121' },
      { date: '2025-12-08', cumulatedAmount: '150' },
      { date: '2025-12-09', cumulatedAmount: '190' },
      { date: '2025-12-10', cumulatedAmount: '220' },
      { date: '2025-12-11', cumulatedAmount: '260' },
      { date: '2025-12-12', cumulatedAmount: '300' },
      { date: '2025-12-13', cumulatedAmount: '330' },
      { date: '2025-12-14', cumulatedAmount: '370' },
      { date: '2025-12-15', cumulatedAmount: '420' },
      { date: '2025-12-16', cumulatedAmount: '480' },
      { date: '2025-12-17', cumulatedAmount: '540' },
      { date: '2025-12-18', cumulatedAmount: '610' },
      { date: '2025-12-19', cumulatedAmount: '650' },
      { date: '2025-12-20', cumulatedAmount: '680' },
      { date: '2025-12-21', cumulatedAmount: '710' },
      { date: '2025-12-22', cumulatedAmount: '760' },
      { date: '2025-12-23', cumulatedAmount: '810' },
      { date: '2025-12-24', cumulatedAmount: '860' },
      { date: '2025-12-25', cumulatedAmount: '930' },
      { date: '2025-12-26', cumulatedAmount: '990' },
      { date: '2025-12-27', cumulatedAmount: '1100' },
      { date: '2025-12-28', cumulatedAmount: '1300' },
      { date: '2025-12-29', cumulatedAmount: '1600' },
      { date: '2025-12-30', cumulatedAmount: '2000' },
      { date: '2025-12-31', cumulatedAmount: '2311.46' },
    ] as Array<MyselfItem>,
  },
  compareByCategory: {
    maxDiffCategoryIndex: 4,
    isOverSpent: true,
    maxLabel: '840',
    items: [
      {
        categoryIndex: 1,
        mySpentAmount: '302.62',
        averageSpentAmount: '379.21',
      },
      {
        categoryIndex: 3,
        mySpentAmount: '210.1',
        averageSpentAmount: '180.05',
      },
      {
        categoryIndex: 4,
        mySpentAmount: '480',
        averageSpentAmount: '300',
      },
      {
        categoryIndex: 6,
        mySpentAmount: '150',
        averageSpentAmount: '220',
      },
      {
        categoryIndex: 2,
        mySpentAmount: '600',
        averageSpentAmount: '650',
      },
      {
        categoryIndex: 5,
        mySpentAmount: '280',
        averageSpentAmount: '240',
      },
      {
        categoryIndex: 7,
        mySpentAmount: '60',
        averageSpentAmount: '55',
      },
      {
        categoryIndex: 8,
        mySpentAmount: '120',
        averageSpentAmount: '130',
      },
    ] as Array<CategoryItem>,
  },
} as const;

export default mockData;
