import type { DragData } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import { createContext, useContext } from 'react';

interface WidgetContextType {
  isEditMode: boolean;
}

interface WidgetItemContextType {
  dragData?: DragData;
  onRemove?: () => void;
}

export const WidgetContext = createContext<WidgetContextType | null>(null);
export const WidgetItemContext = createContext<WidgetItemContextType>({});

export const useWidgetContext = () => {
  const context = useContext(WidgetContext);
  if (!context) return { isEditMode: false };
  return context;
};

export const useWidgetItemContext = () => {
  return useContext(WidgetItemContext);
};
