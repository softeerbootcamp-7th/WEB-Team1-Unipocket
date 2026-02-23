export const ERROR_MESSAGE = {
  LENGTH13: '공백 포함 최대 13자까지 입력 가능합니다.',
  INVALID_CHAR: '허용되지 않는 문자가 포함되어 있습니다.',
} as const;

export const PAGE_TITLE = {
  TRAVEL: {
    title: '여행 포켓',
    subtitle: '여행별로 지출을 정리해두는 공간이에요',
  },
  REPORT: {
    title: '분석',
    subtitle:
      '내 월별 소비를 같은 국가 교환학생과 비교하고 전월 대비 현재 지출 변화를 살펴봐요',
  },
} as const;
