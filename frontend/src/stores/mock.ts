import type { AccountBookMeta } from '@/types/accountBook';

// API 연동 전까지 목업 데이터로 사용. 연동 시 해당 파일 삭제 예정

export const mockData: AccountBookMeta = {
  id: 1,
  title: '🇯🇵 오사카 먹방 여행',
  localCountryCode: 'JP',
  baseCountryCode: 'KR',
  startDate: '2024-02-10',
  endDate: '2024-02-14',
};
