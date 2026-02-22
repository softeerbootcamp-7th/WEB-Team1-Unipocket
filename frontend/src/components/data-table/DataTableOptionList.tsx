import { type ReactNode, useEffect, useRef } from 'react';
import { clsx } from 'clsx';

import { Checkbox } from '@/components/ui/checkbox';

interface DataTableOptionListProps<T> {
  items: T[];
  activeIndex: number;
  setActiveIndex: (index: number) => void;
  isSelected: (item: T) => boolean;
  onSelect: (item: T) => void;
  renderItem: (item: T) => ReactNode;
  customEmptyContent?: ReactNode;
  footer?: ReactNode;
}

export const DataTableOptionList = <T,>({
  items,
  activeIndex,
  setActiveIndex,
  isSelected,
  onSelect,
  renderItem,
  customEmptyContent,
  footer,
}: DataTableOptionListProps<T>) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  // activeIndex가 변경될 때마다 해당 요소로 스크롤을 이동
  useEffect(() => {
    if (!scrollRef.current) return;

    const activeElement = scrollRef.current.children[
      activeIndex
    ] as HTMLElement;

    if (activeElement) {
      // scrollIntoView를 사용해 화면 안으로 끌어옵니다.
      // block: 'nearest'를 하면 딱 필요한 만큼만 최소한으로 스크롤됩니다.
      activeElement.scrollIntoView({ block: 'nearest' });
    }
  }, [activeIndex]);

  return (
    <div className="flex max-h-85 min-h-42 flex-col justify-between p-3">
      <div ref={scrollRef} className="scrollbar overflow-y-auto">
        {customEmptyContent ? (
          customEmptyContent
        ) : items.length > 0 ? (
          items.map((item, index) => (
            <label
              key={String(item)}
              onMouseEnter={() => setActiveIndex(index)}
              className={clsx(
                'group rounded-modal-6 flex cursor-pointer items-center gap-2.5 px-3 py-2 transition-colors',
                activeIndex === index && 'bg-background-alternative',
              )}
              onClick={(e) => {
                e.preventDefault();
                onSelect(item);
              }}
            >
              <Checkbox checked={isSelected(item)} tabIndex={-1} />
              {renderItem(item)}
            </label>
          ))
        ) : (
          <div className="text-label-assistive p-4 text-center text-sm">
            검색 결과가 없습니다.
          </div>
        )}
      </div>
      {footer}
    </div>
  );
};
