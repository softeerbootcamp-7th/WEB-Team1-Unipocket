import clsx from 'clsx';

import { CATEGORY_STYLE, type CategoryType } from '@/types/category';

import { Icons } from '@/assets';

interface ChipProps {
  type: CategoryType | string;
  onRemove?: () => void;
}

const Chip = ({ type, onRemove }: ChipProps) => {
  const { bg, text } = CATEGORY_STYLE[type as CategoryType] || {
    bg: 'bg-label-alternative/10',
    text: 'text-label-alternative',
  };

  return (
    <div
      className={clsx('inline-flex items-center rounded-md px-1.5 py-0.75', bg)}
    >
      <span className={clsx('caption2-bold', text)}>{type}</span>
      {onRemove && (
        <button
          onClick={(e) => {
            e.stopPropagation(); // 클릭 시 인풋 포커스 로직과 겹치지 않도록 방지
            onRemove?.();
          }}
          onMouseDown={(e) => e.preventDefault()} // DataTableFilter의 toggleOption에서 focus()를 다시 호출할 필요가 없어짐.
          className={clsx('cursor-pointer rounded-full p-0.5', text)}
        >
          <Icons.Close className="size-3.5" />
        </button>
      )}
    </div>
  );
};

export default Chip;
