export const ENDPOINTS = {
  AUTH: {
    LOGOUT: 'auth/logout',
    LOGIN_DEV: 'dev/token',
  },
  USERS: {
    ME: 'users/me',
  },
  ACCOUNT_BOOKS: {
    BASE: 'account-books', // GET, POST
    DETAIL: (
      accountBookId: number | string, // GET, PUT, DELETE
    ) => `account-books/${accountBookId}`,
  },
  EXPENSES: {
    BASE: (accountBookId: number | string) =>
      `account-books/${accountBookId}/expenses`, // GET
    MANUAL_UPLOAD: (accountBookId: number | string) =>
      `account-books/${accountBookId}/expenses/manual`, // POST
    DETAIL: (accountBookId: number | string, expenseId: number | string) =>
      `account-books/${accountBookId}/expenses/${expenseId}`, // GET, PUT, DELETE
  },
} as const;
