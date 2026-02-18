import clsx from 'clsx';

import { CATEGORIES } from '@/types/category';

import { Icons } from '@/assets';

interface ChipProps {
  label: string;
  bg?: string;
  text?: string;
  onRemove?: () => void;
}

const Chip = ({
  label,
  bg = CATEGORIES[0]?.bg,
  text = CATEGORIES[0]?.text,
  onRemove,
}: ChipProps) => {
  return (
    <div
      className={clsx('inline-flex items-center rounded-md px-1.5 py-0.75', bg)}
    >
      <span className={clsx('caption2-bold', text)}>{label}</span>
      {onRemove && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            onRemove?.();
          }}
          onMouseDown={(e) => e.preventDefault()}
          className={clsx('cursor-pointer rounded-full p-0.5', text)}
        >
          <Icons.Close className="size-3.5" />
        </button>
      )}
    </div>
  );
};

export default Chip;
