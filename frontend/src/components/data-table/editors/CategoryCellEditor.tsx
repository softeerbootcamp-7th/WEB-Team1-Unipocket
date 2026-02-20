import { useEffect, useRef, useState } from 'react';
import { clsx } from 'clsx'; // clsx 추가 필요

import { useSearchNavigation } from '@/hooks/useSearchNavigation';

import { CategoryChip } from '@/components/common/Chip';
import { useDataTable } from '@/components/data-table/context';
import type { ActiveCellState } from '@/components/data-table/type';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { CATEGORIES, type CategoryId } from '@/types/category';

const CategoryCellEditor = () => {
  const { tableState } = useDataTable();
  const { categoryCell } = tableState;

  if (!categoryCell) return null;

  return (
    <CategoryCellEditorContent
      key={`${categoryCell.rowId}-${categoryCell.columnId}`}
      categoryCell={categoryCell}
    />
  );
};

const CategoryCellEditorContent = ({
  categoryCell,
}: {
  categoryCell: ActiveCellState;
}) => {
  const { dispatch } = useDataTable();
  // 단일 선택이므로 null 허용
  const [categoryId, setCategoryId] = useState<CategoryId | null>(
    categoryCell.value as CategoryId | null,
  );

  const inputRef = useRef<HTMLInputElement>(null);
  const triggerRef = useRef<HTMLDivElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const scrollContainer = document.querySelector(
      '[data-slot="table-container"]',
    );

    if (scrollContainer instanceof HTMLElement) {
      const originalStyle = scrollContainer.style.overflow;
      scrollContainer.style.overflow = 'hidden';

      inputRef.current?.focus();
      inputRef.current?.select();

      return () => {
        scrollContainer.style.overflow = originalStyle;
      };
    }
  }, []);

  const handleSave = () => {
    // TODO: 선택된 categoryId를 서버나 전역 상태에 저장하는 로직 필요
    dispatch({ type: 'SET_CATEGORY_CELL', payload: null });
  };

  const {
    searchTerm,
    handleSearchChange,
    activeIndex,
    setActiveIndex,
    filteredOptions,
    handleKeyDown,
  } = useSearchNavigation<CategoryId>({
    options: Object.keys(CATEGORIES) as unknown as CategoryId[],
    filterFn: (id, term) =>
      CATEGORIES[id].name.toLowerCase().includes(term.toLowerCase()),
    onSelect: (selectedId) => {
      setCategoryId(selectedId);
      // 필요 시 여기서 handleSave() 호출
    },
    onBackspace: () => {
      if (categoryId) setCategoryId(null);
    },
  });

  return (
    <Popover open={true} onOpenChange={(open) => !open && handleSave()}>
      <PopoverTrigger asChild>
        <div
          ref={triggerRef}
          style={{
            position: 'fixed',
            top: categoryCell.rect.top,
            left: categoryCell.rect.left,
            width: categoryCell.rect.width,
            height: categoryCell.rect.height,
            opacity: 0,
          }}
        />
      </PopoverTrigger>

      <PopoverContent
        align="start"
        sideOffset={0}
        className="rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex w-75 flex-col p-0"
        onInteractOutside={() => handleSave()}
        onKeyDown={(e) => {
          if (e.key === 'Escape') handleSave();
        }}
        onOpenAutoFocus={(e) => {
          e.preventDefault();
          inputRef.current?.focus();
        }}
      >
        {/* --- 1. 상단 인풋 영역 (Chip 표시 및 검색) --- */}
        <div
          className="bg-fill-normal border-line-solid-normal relative flex cursor-text flex-wrap items-center gap-2.5 border-b px-3 py-2"
          onClick={() => inputRef.current?.focus()}
        >
          {/* 선택된 카테고리가 있으면 칩(태그)으로 렌더링 */}
          {categoryId && (
            <div className="shrink-0">
              <CategoryChip
                categoryId={categoryId}
                onRemove={() => setCategoryId(null)}
              />
            </div>
          )}

          {/* 실제 검색 인풋 */}
          <input
            ref={inputRef}
            type="text"
            value={searchTerm}
            onChange={(e) => handleSearchChange(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={categoryId ? '' : '카테고리를 입력하세요...'}
            className="placeholder:text-label-assistive caption1-medium min-w-0 flex-1 bg-transparent pt-1 outline-none"
            autoComplete="off"
          />
        </div>

        {/* --- 2. 리스트 영역 (필터링 및 Hover 지원) --- */}
        <div
          ref={listRef}
          className="flex max-h-85 min-h-42 flex-col justify-between p-3"
        >
          <div className="scrollbar overflow-y-auto">
            {filteredOptions.length > 0 ? (
              filteredOptions.map((currentCatId, index) => {
                const isSelected = categoryId === currentCatId;

                return (
                  <label
                    key={currentCatId}
                    onMouseEnter={() => setActiveIndex(index)}
                    className={clsx(
                      'group rounded-modal-6 flex cursor-pointer items-center gap-2.5 px-3 py-2 transition-colors',
                      activeIndex === index && 'bg-background-alternative',
                    )}
                    onClick={(e) => {
                      e.preventDefault();
                      setCategoryId(currentCatId);
                      handleSearchChange(''); // 선택 후 검색어 지우기
                    }}
                  >
                    <Checkbox checked={isSelected} />
                    <CategoryChip categoryId={currentCatId} />
                  </label>
                );
              })
            ) : (
              <div className="text-label-assistive p-4 text-center text-sm">
                검색 결과가 없습니다.
              </div>
            )}
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default CategoryCellEditor;
