interface VerticalGridProps {
  steps: number;
  labels: (number | string)[];
}

const VerticalGrid = ({ steps, labels }: VerticalGridProps) => {
  if (steps <= 0) return null;

  return (
    <div className="pointer-events-none absolute inset-0 left-14.75 flex h-full flex-col gap-4 pr-5">
      <div className="relative flex-1">
        {Array.from({ length: steps + 1 }).map((_, i) => (
          <div
            key={i}
            className="border-line-normal-normal absolute top-0 h-full border-l border-dashed"
            style={{
              left: `${(i / steps) * 100}%`,
            }}
          />
        ))}
      </div>
      <div className="relative">
        {labels.map((label, i) => (
          <div
            key={i}
            className="text-label-assistive absolute text-xs"
            style={{
              left: `${(i / steps) * 100}%`,
              transform: 'translateX(-50%)',
            }}
          >
            {typeof label === 'number'
              ? Math.round(label).toLocaleString()
              : label}
          </div>
        ))}
      </div>
    </div>
  );
};

export default VerticalGrid;
