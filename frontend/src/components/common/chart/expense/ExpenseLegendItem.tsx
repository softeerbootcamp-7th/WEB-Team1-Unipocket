interface ExpenseLegendItemProps {
  label: string;
  currencySignAndName?: string;
  percent: number;
  color: string;
}

const ExpenseLegendItem = ({
  label,
  currencySignAndName,
  percent,
  color,
}: ExpenseLegendItemProps) => {
  return (
    <div className="flex w-40 items-center justify-between">
      <div className="text-label-neutral flex items-center gap-1.75">
        <div className="h-3.5 w-3.5" style={{ backgroundColor: color }} />
        {currencySignAndName && (
          <span className="caption2-medium">{currencySignAndName}</span>
        )}
        <span className="caption1-medium max-w-15 truncate">{label}</span>
      </div>
      <span className="figure-caption1-medium text-label-alternative">
        {percent}%
      </span>
    </div>
  );
};

export default ExpenseLegendItem;
