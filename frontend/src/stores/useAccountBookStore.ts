import { create } from 'zustand';
import { createJSONStorage, devtools, persist } from 'zustand/middleware';

import type { AccountBookMeta } from '@/types/accountBook';

interface AccountBookState {
  accountBook: AccountBookMeta | null;

  setAccountBook: (data: AccountBookMeta) => void;
  updateAccountBook: (updateField: Partial<AccountBookMeta>) => void;
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
