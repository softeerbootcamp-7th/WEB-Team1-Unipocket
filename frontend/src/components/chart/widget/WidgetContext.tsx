import { createContext, useContext } from 'react';

import type { DragData } from '@/components/chart/widget/hook/useWidgetDragAndDrop';

interface WidgetContextType {
  isWidgetEditMode: boolean;
  toggleEditMode: () => void;
}

interface WidgetItemContextType {
  dragData?: DragData;
  onRemove?: () => void;
}

export const WidgetContext = createContext<WidgetContextType | null>(null);
export const WidgetItemContext = createContext<WidgetItemContextType>({});

export const useWidgetContext = () => {
  const context = useContext(WidgetContext);
  if (!context) return { isWidgetEditMode: false, toggleEditMode: () => {} };
  return context;
};

export const useWidgetItemContext = () => {
  return useContext(WidgetItemContext);
};
