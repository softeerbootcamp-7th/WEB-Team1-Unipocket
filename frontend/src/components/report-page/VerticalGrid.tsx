import type { ComponentPropsWithoutRef } from 'react';
import { clsx } from 'clsx';

interface VerticalGridProps extends ComponentPropsWithoutRef<'div'> {
  steps?: number;
  labels: (number | string)[];
  positions?: number[]; // 커스텀 위치 배열
}

const VerticalGrid = ({
  steps,
  labels,
  positions,
  className,
}: VerticalGridProps) => {
  const gridPositions =
    positions ||
    (steps !== undefined
      ? Array.from({ length: steps + 1 }, (_, i) => (i / steps) * 100)
      : []);

  if (gridPositions.length === 0) return null;

  return (
    <div
      className={clsx(
        'pointer-events-none absolute inset-0 flex h-full flex-col gap-4 pr-5',
        className,
      )}
    >
      <div className="relative flex-1">
        {gridPositions.map((position, i) => (
          <div
            key={i}
            className="border-line-normal-normal absolute top-0 h-full border-l border-dashed"
            style={{
              left: `${position}%`,
            }}
          />
        ))}
      </div>
      <div className="relative">
        {gridPositions.map((position, i) => {
          const label = labels[i];
          if (label === undefined || label === null) return null;

          return (
            <div
              key={i}
              className="text-label-assistive absolute text-xs whitespace-nowrap"
              style={{
                left: `${position}%`,
                transform: 'translateX(-50%)',
              }}
            >
              {typeof label === 'number'
                ? Math.round(label).toLocaleString()
                : label}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default VerticalGrid;
