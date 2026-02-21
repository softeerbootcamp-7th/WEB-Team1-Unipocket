import { create } from 'zustand';
import { createJSONStorage, devtools, persist } from 'zustand/middleware';

import type { GetAccountBookDetailResponse } from '@/api/account-books/type';

interface AccountBookState {
  accountBook: GetAccountBookDetailResponse | null;
  setAccountBook: (data: GetAccountBookDetailResponse) => void;
  updateAccountBook: (
    updateField: Partial<GetAccountBookDetailResponse>,
  ) => void;
  clearAccountBook: () => void;
}

export const useAccountBookStore = create<AccountBookState>()(
  devtools(
    persist(
      (set) => ({
        accountBook: null,
        setAccountBook: (data) => set({ accountBook: data }),
        updateAccountBook: (updateField) =>
          set((state) => ({
            accountBook: state.accountBook
              ? { ...state.accountBook, ...updateField }
              : null,
          })),
        clearAccountBook: () => set({ accountBook: null }),
      }),
      {
        name: 'account-book', // sessionStorage key name
        storage: createJSONStorage(() => sessionStorage),
      },
    ),
    { name: 'AccountBookStore' }, // devtools name
  ),
);

export const useRequiredAccountBook = () => {
  const accountBook = useAccountBookStore((s) => s.accountBook);

  if (!accountBook) {
    throw new Error('AccountBook is required in _app route');
  }

  return accountBook;
};

export const useAccountBookCountryCode = (currencyType: 'LOCAL' | 'BASE') => {
  const { localCountryCode, baseCountryCode } = useRequiredAccountBook();
  return currencyType === 'LOCAL' ? localCountryCode : baseCountryCode;
};
