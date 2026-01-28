import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface AuthState {
  isAuthenticated: boolean;
  setAuthenticated: (isAuth: boolean) => void;
}

const useAuthStore = create<AuthState>()(
  // persist -> 상태를 브라우저 저장소(LocalStorage 등)에 자동으로 저장
  persist(
    (set) => ({
      isAuthenticated: false,
      setAuthenticated: (isAuth) => set({ isAuthenticated: isAuth }),
    }),
    {
      name: 'auth-storage', // 로컬 스토리지에 저장될 키 이름
    },
  ),
);

export default useAuthStore;
