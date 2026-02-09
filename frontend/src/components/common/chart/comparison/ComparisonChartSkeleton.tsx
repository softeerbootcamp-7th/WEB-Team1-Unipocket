const ComparisonChartSkeleton = () => {
  return (
    <>
      <div className="flex flex-col gap-2.5">
        <div className="bg-fill-strong rounded-modal-4 h-4.5 w-48.5 animate-pulse" />
        <div className="bg-fill-strong rounded-modal-4 h-4.5 w-41 animate-pulse" />
      </div>
      <div className="flex h-26.5 flex-col gap-3">
        <div className="bg-fill-strong rounded-modal-4 h-2.75 w-13 animate-pulse" />
        <div className="flex h-8.5 gap-3.5">
          <div className="bg-fill-strong h-8 w-21.25 animate-pulse rounded-xs" />
          <div className="flex flex-1 flex-col gap-1.5">
            <div className="bg-fill-normal rounded-modal-4 h-3.25 w-21.25 animate-pulse" />
            <div className="bg-fill-normal rounded-modal-4 h-3.25 w-26 animate-pulse" />
          </div>
        </div>
        <div className="flex h-8.5 gap-3.5">
          <div className="bg-fill-strong h-8 w-17 animate-pulse rounded-xs" />
          <div className="flex flex-1 flex-col gap-1.5">
            <div className="bg-fill-normal rounded-modal-4 h-3.25 w-2.75 animate-pulse" />
            <div className="bg-fill-normal rounded-modal-4 h-3.25 w-26 animate-pulse" />
          </div>
        </div>
      </div>
    </>
  );
};

export default ComparisonChartSkeleton;
