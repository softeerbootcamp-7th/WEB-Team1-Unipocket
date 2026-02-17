import clsx from 'clsx';

interface GapDropIndicatorProps {
  position: 'left' | 'right';
}

const HALF_GAP = 10;

const GapDropIndicator = ({ position }: GapDropIndicatorProps) => {
  const isGapDragOver = true;

  return (
    <div
      className={clsx(
        'bg-primary-normal/50 absolute top-0 bottom-0 z-60 flex w-20 items-center justify-center',
        position === 'left' ? 'left-0' : 'right-0',
      )}
      style={{
        transform: `translateX(${position === 'left' ? '-50%' : '50%'}) translateX(${position === 'left' ? -HALF_GAP : HALF_GAP}px)`,
      }}
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
