import { createContext, useContext } from 'react';

interface WidgetContextType {
  isEditMode: boolean;
  // 추후 위젯 삭제 핸들러 등 추가 예정
  // onDeleteWidget?: (id: number) => void;
}

export const WidgetContext = createContext<WidgetContextType | null>(null);

export const useWidgetContext = () => {
  const context = useContext(WidgetContext);
  // Provider 밖에서 사용할 경우를 대비한 안전장치 (선택사항)
  if (!context) return { isEditMode: false };
  return context;
};
