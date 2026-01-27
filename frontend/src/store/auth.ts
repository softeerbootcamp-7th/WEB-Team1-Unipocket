import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  isAuthenticated: boolean;
  hasVisited: boolean;
  setAuthenticated: (isAuth: boolean) => void;
  setHasVisited: (visited: boolean) => void;
}

const useAuth = create<AuthState>()(
  // persist -> 상태를 브라우저 저장소(LocalStorage 등)에 자동으로 저장
  persist(
    (set) => ({
      isAuthenticated: false,
      hasVisited: false,
      setAuthenticated: (isAuth) => set({ isAuthenticated: isAuth }),
      setHasVisited: (visited) => set({ hasVisited: visited }),
    }),
    {
      name: 'auth-storage', // 로컬 스토리지에 저장될 키 이름
      partialize: (state) => ({ hasVisited: state.hasVisited }), // hasVisited만 저장
    },
  ),
);

export default useAuth;
