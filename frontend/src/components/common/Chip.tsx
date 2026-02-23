import clsx from 'clsx';

import { CATEGORIES, CATEGORY_STYLES, type CategoryId } from '@/types/category';

import { Icons } from '@/assets';

interface CategoryChipProps {
  categoryId: CategoryId;
  onRemove?: () => void;
}

export const CategoryChip = ({ categoryId, onRemove }: CategoryChipProps) => {
  const { name } = CATEGORIES[categoryId];
  const { bg, text } = CATEGORY_STYLES[categoryId];

  return (
    <Chip
      label={name}
      bgClassName={bg}
      textClassName={text}
      onRemove={onRemove}
    />
  );
};

interface ChipProps {
  label: string;
  bgClassName?: string;
  textClassName?: string;
  onRemove?: () => void;
  className?: string;
}

const Chip = ({
  label,
  bgClassName = 'bg-label-alternative/10',
  textClassName = 'text-label-alternative',
  onRemove,
  className,
}: ChipProps) => {
  return (
    <div
      className={clsx(
        'inline-flex items-center rounded-md px-1.5 py-0.75',
        bgClassName,
        className,
      )}
    >
      <span className={clsx('caption2-bold shrink-0', textClassName)}>
        {label}
      </span>

      {onRemove && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            onRemove();
          }}
          onMouseDown={(e) => e.preventDefault()}
          className={clsx('cursor-pointer rounded-full p-0.5', textClassName)}
        >
          <Icons.Close className="size-3.5" />
        </button>
      )}
    </div>
  );
};

export default Chip;
