import type { AccountBookMeta } from '@/types/accountBook';

// API 연동 전까지 목업 데이터로 사용. 연동 시 해당 파일 삭제 예정

export const mockData: AccountBookMeta = {
  id: 1,
  title: '🇯🇵 오사카 교환학생',
  localCountryCode: 'JP',
  baseCountryCode: 'KR',
  startDate: '2025-08-10',
  endDate: '2026-03-14',
};
