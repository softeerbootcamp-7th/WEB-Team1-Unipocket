export const ERROR_MESSAGE = {
  LENGTH15: '공백 포함 최대 15자까지 입력 가능합니다.',
  LENGTH30: '최대 30자까지 입력할 수 있어요.',
  INVALID_CHAR: '허용되지 않는 문자가 포함되어 있습니다.',
  ACCOUNT_BOOK_NAME_DUPLICATE: '이미 동일한 이름의 가계부가 있어요.',
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

export const LOGIN_MODAL_TEXT = {
  REACTIVATE_ACCOUNT: {
    title: '계정 복구',
    description: '탈퇴한 계정입니다.',
    subDescription: '계정을 복구하고 \n다시 로그인하시겠습니까?',
    confirmButtonLabel: '복구하기',
  },
} as const;

export const POLICY = {
  TRAVEL_MODAL: '지원 형식: jpg, jpeg, png',
};
