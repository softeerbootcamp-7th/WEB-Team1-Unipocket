import clsx from 'clsx';

const LEGEND_COLOR = {
  me: 'bg-primary-normal',
  other: 'bg-cool-neutral-95',
} as const;

interface ReportLegendProps {
  color: keyof typeof LEGEND_COLOR;
  label: string;
}

const ReportLegend = ({ color, label }: ReportLegendProps) => {
  return (
    <div className="flex items-center gap-1.5">
      <div className={clsx('h-2.5 w-2.5', LEGEND_COLOR[color])} />
      <span className="label1-normal-regular text-label-alternative">
        {label}
      </span>
    </div>
  );
};

export default ReportLegend;