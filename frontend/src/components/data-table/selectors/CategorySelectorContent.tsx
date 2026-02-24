import type { PopoverContentProps } from '@radix-ui/react-popover';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

import { CategoryChip } from '@/components/common/Chip';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import { PopoverContent } from '@/components/ui/popover';

import { CATEGORIES, type CategoryId } from '@/types/category';

interface CategorySelectorContentProps extends PopoverContentProps {
  initialCategoryId: CategoryId | null;
  onCategorySelect: (id: CategoryId) => void;
  onInteractOutside?: () => void;
}

export const CategorySelectorContent = ({
  initialCategoryId,
  onCategorySelect,
  onInteractOutside,
  align = 'start',
  sideOffset = 0,
  className,
  ...props
}: CategorySelectorContentProps) => {
  const options = Object.keys(CATEGORIES) as unknown as CategoryId[];
  const currentCategory = initialCategoryId;

  const initialIndex = Math.max(
    0,
    currentCategory !== null
      ? options.findIndex((opt) => Number(opt) === Number(currentCategory))
      : 0,
  );

  const { activeIndex, setActiveIndex, handleKeyDown } =
    useKeyboardNavigation<CategoryId>({
      items: options,
      initialActiveIndex: initialIndex,
      onSelect: onCategorySelect,
    });

  return (
    <PopoverContent
      align={align}
      sideOffset={sideOffset}
      className={`rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex w-75 flex-col p-0 outline-none ${className || ''}`}
      onInteractOutside={onInteractOutside}
      onKeyDown={handleKeyDown}
      {...props}
    >
      <DataTableOptionList
        items={options}
        activeIndex={activeIndex}
        setActiveIndex={setActiveIndex}
        isSelected={(item) =>
          currentCategory !== null && Number(currentCategory) === Number(item)
        }
        onSelect={onCategorySelect}
        renderItem={(item) => <CategoryChip categoryId={item} />}
      />
    </PopoverContent>
  );
};
