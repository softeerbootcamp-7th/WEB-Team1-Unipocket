import { useEffect, useRef, useState } from 'react';

import { useDataTable } from '@/components/data-table/context';
import type { ActiveCellState } from '@/components/data-table/type';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

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
  const [value, setValue] = useState(String(categoryCell.value));
  const inputRef = useRef<HTMLInputElement>(null);

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
    dispatch({ type: 'SET_CATEGORY_CELL', payload: null });
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && handleClose()}>
      {/* 💡 PopoverTrigger를 셀 위치에 고정
        - triggerRef를 통해 자동으로 클릭되게 하여 팝업을 엽니다.
        - 실제 UI는 보이지 않게 투명하게 처리합니다.
      */}
      <PopoverTrigger asChild>
        <div
          ref={triggerRef}
          style={{
            position: 'fixed',
            top: categoryCell.rect.top,
            left: categoryCell.rect.left,
            width: categoryCell.rect.width,
            height: categoryCell.rect.height,
            pointerEvents: 'none', // 클릭 통과
            opacity: 0, // 숨김
          }}
        />
      </PopoverTrigger>

      {/* 💡 PopoverContent 내부에 DataTableSearchFilter UI를 재구성
        - DataTableSearchFilter 컴포넌트 자체를 사용하기보다, 그 내부 구조를 활용하여
          스크린샷처럼 커스터마이징하는 것이 더 유연할 수 있습니다.
        - 아래는 스크린샷과 최대한 비슷하게 구조를 잡은 예시입니다.
      */}
      <PopoverContent
        align="start"
        sideOffset={0} // 셀 바로 아래 붙기
        className="rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex w-75 flex-col p-0"
        onInteractOutside={(e) => {
          // 팝업 외부 클릭 시 닫기 (저장)
          handleClose();
        }}
        onKeyDown={(e) => {
          if (e.key === 'Escape') handleClose();
        }}
      >
        {/* 1. 상단 인풋 영역 (카테고리 입력하세요...) */}
        <div className="border-line-solid-normal border-b px-3 py-2">
          {/* 💡 실제 인풋 로직은 DataTableSearchFilter의 로직을 참고하여 구현 필요 */}
          <input
            type="text"
            placeholder="카테고리를 입력하세요..."
            className="placeholder:text-label-assistive caption1-medium w-full outline-none"
          />
        </div>

        {/* 2. 안내 문구 및 리스트 영역 */}
        <div className="flex max-h-85 min-h-42 flex-col p-3">
          <div className="text-label-assistive mb-2 text-xs">
            카테고리를 선택하거나 새로 생성하세요.
          </div>
          <div className="scrollbar flex flex-col gap-1 overflow-y-auto">
            {CATEGORY_OPTIONS.map((option) => {
              const isSelected = selectedOptions.includes(option);
              return (
                <label
                  key={option}
                  className="group rounded-modal-6 hover:bg-background-alternative flex cursor-pointer items-center gap-2.5 px-3 py-2 transition-colors"
                  onClick={() => {
                    // 단일 선택 로직 (필요에 따라 다중 선택으로 변경)
                    setSelectedOptions([option]);
                    // 선택 후 바로 닫고 싶으면 handleClose() 호출
                    // handleClose();
                  }}
                >
                  <Checkbox checked={isSelected} />
                  {/* 💡 Tag 컴포넌트 활용 */}
                  <Tag type={option as any} />
                </label>
              );
            })}
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default CategoryCellEditor;
