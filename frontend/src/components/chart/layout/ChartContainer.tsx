import type { ComponentProps } from 'react';

import type { ChartMode } from '@/components/chart/chartType';
import { useWidgetContext } from '@/components/chart/widget/WidgetContext';

import { Icons } from '@/assets';
import { cn } from '@/lib/utils';

// props 추가 시 type -> interface 변경 필요
interface ChartContainerProps extends ComponentProps<'div'>, ChartMode {}

const ChartContainer = ({
  isPreview = false,
  children,
  className,
  ...props
}: ChartContainerProps) => {
  const isDragging = false; // TODO: 드래그 상태 관리

  const { isEditMode } = useWidgetContext();

  return (
    <div
      className={cn(
        'rounded-modal-16 bg-background-normal shadow-semantic-subtle relative flex h-72 w-67 shrink-0 flex-col gap-2.5 p-2 pt-4',
        className,
        isDragging && 'opacity-80',
      )}
      {...props}
    >
      {!isPreview && isEditMode && (
        <Icons.CloseButton className="absolute -top-1.5 -left-2.5 size-7" />
      )}
      {children}
    </div>
  );
};

export default ChartContainer;
