export const SETTING_MODAL_TEXT = {
  EDIT_CARD_NICKNAME: {
    title: '카드 별명 수정',
    description: '카드의 별명을 입력해주세요',
    placeholder: '별명을 입력하세요',
    confirm: '저장',
  },
  EDIT_ACCOUNT_BOOK_NAME: {
    title: '가계부 이름 변경',
    description: '최대 10자까지 입력할 수 있어요',
    placeholder: '최대 10자',
    confirm: '저장',
  },
  DELETE_CARD: {
    title: '카드를 삭제하시겠습니까?',
    description: (nickname: string) =>
      `${nickname} 카드를 정말 삭제하시겠어요?`,
    confirm: '삭제',
  },
  DELETE_ACCOUNT_BOOK: {
    title: '가계부를 삭제하시겠습니까?',
    description: (title: string) => `${title} \n가계부를 정말 삭제하시겠어요?`,
    subDescription: `메인 가계부를 삭제하면 \n다음 가계부가 메인으로 설정돼요.`,
    confirm: '삭제',
  },
  DELETE_ACCOUNT: {
    title: '계정을 삭제하시겠습니까?',
    description: '계정을 삭제하면 모든 데이터가 삭제됩니다.',
    confirm: '삭제',
  },
} as const;
