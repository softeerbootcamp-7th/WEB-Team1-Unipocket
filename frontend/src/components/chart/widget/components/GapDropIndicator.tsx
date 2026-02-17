import clsx from 'clsx';

import {
  type DragData,
  useGapDropZone,
} from '@/components/chart/widget/hook/useWidgetDragAndDrop';

interface GapDropIndicatorProps {
  dropOrder: number;
  onDropToGap: (data: DragData, dropOrder: number) => void;
  position: 'left' | 'right';
}

const HALF_GAP = 10;

const GapDropIndicator = ({
  dropOrder,
  onDropToGap,
  position,
}: GapDropIndicatorProps) => {
  const { isGapDragOver, gapDropProps } = useGapDropZone({
    dropOrder,
    onDropToGap,
  });

  return (
    <div
      className={clsx(
        'bg-primary-normal/50 absolute top-0 bottom-0 z-60 flex w-20 items-center justify-center',
        position === 'left' ? 'left-0' : 'right-0',
      )}
      style={{
        transform: `translateX(${position === 'left' ? '-50%' : '50%'}) translateX(${position === 'left' ? -HALF_GAP : HALF_GAP}px)`,
      }}
      {...gapDropProps}
    >
      <div
        className={clsx(
          'pointer-events-none mx-auto h-36.5 w-1.5 rounded-full transition-colors',
          isGapDragOver ? 'bg-cool-neutral-90' : 'bg-transparent',
        )}
      />
    </div>
  );
};

export default GapDropIndicator;
