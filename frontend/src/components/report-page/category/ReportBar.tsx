import clsx from 'clsx';

interface ReportBarProps {
  value: number;
  variant: 'me' | 'other';
}

const ReportBar = ({ value, variant }: ReportBarProps) => {
  return (
    <div className="flex items-center gap-2">
      <div
        className={clsx(
          'h-3 w-57.75 rounded-full',
          variant === 'me'
            ? 'bg-primary-normal'
            : 'bg-cool-neutral-95',
        )}
      />
      <span className="caption1-medium text-label-neutral">
        {value.toLocaleString()}
      </span>
    </div>
  );
};

export default ReportBar;
