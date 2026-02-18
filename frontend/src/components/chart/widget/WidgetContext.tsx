import { createContext, useContext } from 'react';

import type { DragData } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import { useWidgetManager } from '@/components/chart/widget/hook/useWidgetManager';

type WidgetManagerReturnType = ReturnType<typeof useWidgetManager>;

export const WidgetContext = createContext<WidgetManagerReturnType | null>(
  null,
);

export const useWidgetContext = () => {
  const context = useContext(WidgetContext);

  if (!context) {
    throw new Error(
      'useWidgetContext must be used within a WidgetManageProvider',
    );
  }

  return context;
};

interface WidgetItemContextType {
  dragData?: DragData;
  onRemove?: () => void;
}
export const WidgetItemContext = createContext<WidgetItemContextType>({});
export const useWidgetItemContext = () => useContext(WidgetItemContext);
