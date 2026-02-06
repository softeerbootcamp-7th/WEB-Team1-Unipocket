import type { CategoryStatisticsResponse } from './CategoryChart';

export const mockData: CategoryStatisticsResponse = {
  totalAmount: 10031,
  countryCode: 'US',
  items: [
    {
      categoryName: '거주',
      amount: 450,
      percent: 35,
    },
    {
      categoryName: '식비',
      amount: 250,
      percent: 20,
    },
    {
      categoryName: '쇼핑',
      amount: 100,
      percent: 12,
    },
    {
      categoryName: '학교',
      amount: 50,
      percent: 10,
    },
    {
      categoryName: '생활',
      amount: 100,
      percent: 10,
    },
    {
      categoryName: '통신비',
      amount: 30,
      percent: 8,
    },
    {
      categoryName: '미분류',
      amount: 20,
      percent: 5,
    },
  ],
};
