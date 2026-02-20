export const ENDPOINTS = {
  AUTH: {
    LOGOUT: 'auth/logout', // ✓
    LOGIN_DEV: 'dev/token', // ✓
    REISSUE: 'auth/reissue', // 토큰 재발급 (POST)
    OAUTH_AUTHORIZE: (provider: string) => `auth/oauth2/authorize/${provider}`, // OAuth 인증 시작 (GET)
    OAUTH_CALLBACK: (provider: string) => `auth/oauth2/callback/${provider}`, // OAuth 콜백 (GET)
  },
  USERS: {
    ME: 'users/me', // ✓ (GET)
    WITHDRAW: 'users/me', // 회원 탈퇴 (DELETE)
    CARDS: 'users/cards', // 카드 목록 조회 (GET), 카드 등록 (POST)
    CARD_DETAIL: (cardId: number | string) => `users/cards/${cardId}`, // 카드 삭제 (DELETE)
    CARD_COMPANIES: 'users/cards/companies', // 카드사 목록 조회 (GET)
  },
  TRAVELS: {
    BASE: (accountBookId: number | string) =>
      `account-books/${accountBookId}/travels`, // 여행 목록 조회 (GET), 여행 생성 (POST)

    DETAIL: (accountBookId: number | string, travelId: number | string) =>
      `account-books/${accountBookId}/travels/${travelId}`, // 여행 상세 조회 (GET), 여행 수정 (PUT), 여행 부분 수정 (PATCH), 여행 삭제 (DELETE)

    WIDGETS: (accountBookId: number | string, travelId: number | string) =>
      `account-books/${accountBookId}/travels/${travelId}/widgets`, // 위젯 순서 수정 (PUT)
  },
  TEMPORARY_EXPENSES: {
    METAS: (accountBookId: number | string) =>
      `account-books/${accountBookId}/temporary-expense-metas`, // 메타 목록 조회 (GET)

    META_FILES: (accountBookId: number | string, metaId: number | string) =>
      `account-books/${accountBookId}/temporary-expense-metas/${metaId}/files`, // 파일별 임시지출 조회 (GET)

    META_FILE_DETAIL: (
      accountBookId: number | string,
      metaId: number | string,
      fileId: number | string,
    ) =>
      `account-books/${accountBookId}/temporary-expense-metas/${metaId}/files/${fileId}`, // 파일 단건 조회 (GET)

    PARSE_STATUS: (accountBookId: number | string, taskId: string) =>
      `account-books/${accountBookId}/temporary-expenses/parse-status/${taskId}`, // SSE 진행 상황 (GET)

    PRESIGNED_URL: (accountBookId: number | string) =>
      `account-books/${accountBookId}/temporary-expenses/uploads/presigned-url`, // Presigned URL 발급 (POST)

    PARSE: (accountBookId: number | string) =>
      `account-books/${accountBookId}/temporary-expenses/parse`, // 파싱 시작 (POST)

    CONFIRM: (accountBookId: number | string, metaId: number | string) =>
      `account-books/${accountBookId}/temporary-expense-metas/${metaId}/confirm`, // 임시지출 확정 (POST)

    BULK_UPDATE: (
      accountBookId: number | string,
      metaId: number | string,
      fileId: number | string,
    ) =>
      `account-books/${accountBookId}/temporary-expense-metas/${metaId}/files/${fileId}/temporary-expenses`, // 임시지출 일괄 수정 (PATCH)

    DELETE_META: (accountBookId: number | string, metaId: number | string) =>
      `account-books/${accountBookId}/temporary-expense-metas/${metaId}`, // 메타 삭제 (DELETE)
  },
  WIDGETS: {
    ACCOUNT_BOOK_DATA: (accountBookId: number | string) =>
      `account-books/${accountBookId}/widget`, // 가계부 위젯 데이터 조회 (GET)

    ACCOUNT_BOOK_LAYOUT: (accountBookId: number | string) =>
      `account-books/${accountBookId}/widgets`, // 가계부 위젯 순서 조회 (GET), 순서 수정 (PUT)

    TRAVEL_DATA: (accountBookId: number | string, travelId: number | string) =>
      `account-books/${accountBookId}/travels/${travelId}/widget`, // 여행 위젯 데이터 조회 (GET)

    TRAVEL_LAYOUT: (
      accountBookId: number | string,
      travelId: number | string,
    ) => `account-books/${accountBookId}/travel/${travelId}/widgets`, // 여행 위젯 순서 조회 (GET), 순서 수정 (PUT)
  },
  ANALYSIS: {
    MONTHLY_SUMMARY: (accountBookId: number | string) =>
      `account-books/${accountBookId}/analysis/monthly-summary`, // 월 지출 요약 조회 (GET)

    CATEGORY_BREAKDOWN: (accountBookId: number | string) =>
      `account-books/${accountBookId}/analysis/category-breakdown`, // 카테고리 지출 분해 조회 (GET)
  },
  EXPENSES: {
    BASE: (accountBookId: number | string) =>
      `account-books/${accountBookId}/expenses`,
    MANUAL_UPLOAD: (accountBookId: number | string) =>
      `account-books/${accountBookId}/expenses/manual`,
    DETAIL: (accountBookId: number | string, expenseId: number | string) =>
      `account-books/${accountBookId}/expenses/${expenseId}`,
    FILE_URL: (accountBookId: number | string, expenseId: number | string) =>
      `account-books/${accountBookId}/expenses/${expenseId}/file-url`,
    MERCHANT_SEARCH: (accountBookId: number | string) =>
      `account-books/${accountBookId}/expenses/merchant-names`, // 거래처명 자동완성 검색 (GET)
  },
  ACCOUNT_BOOKS: {
    BASE: 'account-books', // 가계부 목록 조회 (GET), 가계부 생성 (POST)

    DETAIL: (accountBookId: number | string) =>
      `account-books/${accountBookId}`, // 가계부 상세 조회 (GET), 가계부 수정 (PATCH), 가계부 삭제 (DELETE)

    BUDGET: (accountBookId: number | string) =>
      `account-books/${accountBookId}/budget`, // 가계부 예산 수정 (PATCH)

    EXCHANGE_RATE: (accountBookId: number | string) =>
      `account-books/${accountBookId}/exchange-rate`, // 가계부 환율 조회 (GET)
  },
} as const;
