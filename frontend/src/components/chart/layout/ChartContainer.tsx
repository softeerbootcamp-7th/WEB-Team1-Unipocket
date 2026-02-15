import type { ComponentProps } from 'react';

import { useDraggable } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import {
  useWidgetContext,
  useWidgetItemContext,
} from '@/components/chart/widget/WidgetContext';

import { Icons } from '@/assets';
import { cn } from '@/lib/utils';

interface ChartContainerProps extends ComponentProps<'div'> {
  isPreview?: boolean;
}

const ChartContainer = ({
  isPreview = false,
  children,
  className,
  ...props
}: ChartContainerProps) => {
  const { isEditMode } = useWidgetContext();
  const { dragData, onRemove } = useWidgetItemContext();
  const { isDragging, dragHandleProps } = useDraggable({
    dragData: dragData,
    isDraggable: isEditMode && !!dragData,
  });

  return (
    <div
      className={cn(
        'rounded-modal-16 bg-background-normal shadow-semantic-subtle relative flex h-72 w-67 shrink-0 flex-col gap-2.5 p-2 pt-4',
        className,
        isDragging && 'opacity-0.5', // 드래그 전 위치에서는 거의 안보이도록 처리
        isEditMode && dragData && 'cursor-grab',
        isEditMode && 'shadow-semantic-emphasize',
      )}
      {...props}
      {...dragHandleProps}
    >
      {!isPreview && isEditMode && !isDragging && (
        <Icons.CloseButton
          className="absolute -top-1.5 -left-2.5 size-7 cursor-pointer"
          onClick={(e) => {
            e.stopPropagation();
            onRemove?.();
          }}
        />
      )}
      {children}
    </div>
  );
};

export default ChartContainer;
