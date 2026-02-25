import { create } from 'zustand';

import type { SnackbarStatus } from '@/components/common/Snackbar';

type ParseType = 'file' | 'image';

export interface SnackbarEntry {
  id: string;
  status: SnackbarStatus;
  description?: string;
  parsedMetaId?: number;
  parseType?: ParseType;
  accountBookId?: number;
  isResultModalOpen: boolean;
}

interface ParseSnackbarState {
  snackbars: SnackbarEntry[];
  addSnackbar: (entry: Omit<SnackbarEntry, 'isResultModalOpen'>) => void;
  updateSnackbar: (
    id: string,
    payload: Partial<Omit<SnackbarEntry, 'id' | 'isResultModalOpen'>>,
  ) => void;
  closeSnackbar: (id: string) => void;
  openResultModal: (id: string) => void;
  closeResultModal: (id: string) => void;
  resetAll: () => void;
}

export const useParseSnackbarStore = create<ParseSnackbarState>((set) => ({
  snackbars: [],
  addSnackbar: (entry) =>
    set((state) => {
      const exists = state.snackbars.some((s) => s.id === entry.id);
      if (exists) {
        return {
          snackbars: state.snackbars.map((s) =>
            s.id === entry.id ? { ...s, ...entry } : s,
          ),
        };
      }
      return {
        snackbars: [...state.snackbars, { ...entry, isResultModalOpen: false }],
      };
    }),
  updateSnackbar: (id, payload) =>
    set((state) => ({
      snackbars: state.snackbars.map((s) =>
        s.id === id ? { ...s, ...payload } : s,
      ),
    })),
  closeSnackbar: (id) =>
    set((state) => ({
      snackbars: state.snackbars.filter((s) => s.id !== id),
    })),
  openResultModal: (id) =>
    set((state) => ({
      snackbars: state.snackbars.map((s) =>
        s.id === id ? { ...s, isResultModalOpen: true } : s,
      ),
    })),
  closeResultModal: (id) =>
    set((state) => ({
      // 결과 모달이 닫히면 해당 스낵바 항목 제거
      snackbars: state.snackbars.filter((s) => s.id !== id),
    })),
  resetAll: () => set({ snackbars: [] }),
}));
