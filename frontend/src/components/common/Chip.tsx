import clsx from 'clsx';

import {
  CATEGORY_STYLES,
  type CategoryId,
  getCategoryName,
} from '@/types/category';

import { Icons } from '@/assets';

interface ChipProps {
  id?: CategoryId;
  label?: string;
  onRemove?: () => void;
}

const Chip = ({ id, label, onRemove }: ChipProps) => {
  const targetId = id ?? 0;

  // ID가 있으면 카테고리 이름, 없으면 전달받은 label, 둘 다 없으면 '미분류'
  const displayLabel =
    id !== undefined ? getCategoryName(id) : (label ?? getCategoryName(0));

  const { bg, text } = CATEGORY_STYLES[targetId];

  return (
    <div
      className={clsx('inline-flex items-center rounded-md px-1.5 py-0.75', bg)}
    >
      <span className={clsx('caption2-bold', text)}>{displayLabel}</span>
      {onRemove && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onRemove();
          }}
          onMouseDown={(e) => e.preventDefault()}
          className={clsx(
            'cursor-pointer rounded-full p-0.5 transition-opacity hover:opacity-70',
            text,
          )}
        >
          <Icons.Close className="size-3.5" />
        </button>
      )}
    </div>
  );
};

export default Chip;
