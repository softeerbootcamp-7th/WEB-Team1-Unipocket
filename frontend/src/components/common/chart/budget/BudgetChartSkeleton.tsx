import SemiCircleChart from '../charts/SemiCircleChart';

const BudgetChartSkeleton = () => {
  return (
    <div className="flex flex-col gap-3">
      <div className="flex w-full flex-col items-start gap-3">
        <div className="bg-fill-normal rounded-modal-2 flex h-3 w-22 animate-pulse" />
        <div className="flex w-full flex-col items-start gap-2">
          <div className="bg-fill-strong rounded-modal-2 flex h-6 w-38 animate-pulse" />
          <div className="bg-fill-strong rounded-modal-2 flex h-3 w-25 animate-pulse" />
        </div>
      </div>

      <SemiCircleChart
        value={80}
        color="var(--color-cool-neutral-90)"
        animate={false}
      >
        <div className="text-label-neutral flex flex-col items-center justify-end gap-3 pb-1">
          <div className="bg-fill-normal rounded-modal-2 flex h-4.5 w-11 animate-pulse" />
          <div className="bg-fill-normal rounded-modal-2 flex h-3 w-6 animate-pulse" />
        </div>
      </SemiCircleChart>
    </div>
  );
};

export default BudgetChartSkeleton;
