import clsx from 'clsx';

const LEGEND_COLOR = {
  me: 'bg-primary-normal',
  other: 'bg-cool-neutral-95',
} as const;

const SHAPE_STYLE = {
  box: 'size-2.5',
  line: 'w-6.25 h-1 rounded-modal-10',
} as const;

interface ReportLegendProps {
  color: keyof typeof LEGEND_COLOR;
  label: string;
  variant?: 'box' | 'line';
}

const ReportLegend = ({ color, label, variant = 'box' }: ReportLegendProps) => {
  return (
    <div className="flex items-center gap-1.5">
      <div className={clsx(SHAPE_STYLE[variant], LEGEND_COLOR[color])} />
      <span className="label1-normal-regular text-label-alternative">
        {label}
      </span>
    </div>
  );
};

export default ReportLegend;
