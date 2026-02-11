interface VerticalGridProps {
  steps: number;
  maxLabel: number;
}

const VerticalGrid = ({ steps, maxLabel }: VerticalGridProps) => {
  if (steps <= 0) {
    return null;
  }

  const values = Array.from(
    { length: steps + 1 },
    (_, i) => (maxLabel / steps) * i,
  );

  return (
    <div className="pointer-events-none absolute inset-0 left-14.75 flex h-full flex-col gap-4 pr-5">
      <div className="relative flex-1">
        {values.map((_, i) => (
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
        {values.map((value, i) => (
          <div
            key={i}
            className="text-label-assistive absolute text-xs"
            style={{
              left: `${(i / steps) * 100}%`,
              transform: 'translateX(-50%)',
            }}
          >
            {Math.round(value).toLocaleString()}
          </div>
        ))}
      </div>
    </div>
  );
};

export default VerticalGrid;
