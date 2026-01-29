import { createContext, useContext } from 'react';

// 1. Context 정의 (자식 컴포넌트에서 setActionReady를 쓰기 위함)
interface ModalContextType {
  setActionReady: (isActionReady: boolean) => void;
}

export const ModalContext = createContext<ModalContextType | null>(null);

// 2. Custom Hook: 자식 컴포넌트가 사용할 훅
export const useModalContext = () => {
  const context = useContext(ModalContext);
  if (!context) {
    throw new Error('useModal must be used within a Modal');
  }
  return context;
};
