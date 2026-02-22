import { type ComponentPropsWithoutRef, useRef, useState } from 'react';

import { useSearchNavigation } from '@/hooks/useSearchNavigation';

import Chip, { CategoryChip } from '@/components/common/Chip';
import Filter from '@/components/common/Filter';
import { DataTableFilterContext } from '@/components/data-table/context';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { CATEGORIES, type CategoryId } from '@/types/category';

import type { ExpenseSearchFilter } from '@/api/expenses/type';

interface DataTableFilterProviderProps extends ComponentPropsWithoutRef<'div'> {
  filter: ExpenseSearchFilter;
  updateFilter: (newFilter: Partial<ExpenseSearchFilter>) => void;
}

const DataTableFilterProvider = ({
  filter,
  updateFilter,
  children,
  ...props
}: DataTableFilterProviderProps) => {
  return (
    <DataTableFilterContext.Provider value={{ filter, updateFilter }}>
      <div className="mb-5 flex items-center gap-3 px-2.5" {...props}>
        {children}
      </div>
    </DataTableFilterContext.Provider>
  );
};

interface DataTableSearchFilterProps<T> {
  title: string;
  options: T[];
  selectedOptions: T[];
  setSelectedOptions: (selected: T[]) => void;
  onInputChange?: (term: string) => void;
  onSelect?: (term: T) => void;
  onSelectMultiple?: (terms: T[]) => void;
  isCategory?: boolean;
  filterFn?: (option: T, searchTerm: string) => boolean;
  // 렌더링 옵션
  renderOption: (option: T, searchTerm: string) => React.ReactNode;
  renderEmptyState?: () => React.ReactNode;
  renderSearchAllTrigger?: (
    searchTerm: string,
    onSelectAll: () => void,
  ) => React.ReactNode;
  getDisplayLabel?: (option: T) => string;
}

const DataTableSearchFilter = <T,>({
  title,
  options,
  selectedOptions,
  setSelectedOptions,
  renderOption,
  filterFn,
  renderEmptyState,
  isCategory = false,
  renderSearchAllTrigger,
  onInputChange,
  onSelect,
  onSelectMultiple,
  getDisplayLabel,
}: DataTableSearchFilterProps<T>) => {
  const [isOpen, setIsOpen] = useState(false);

  const inputRef = useRef<HTMLInputElement>(null);

  const toggleOption = (option: T) => {
    if (selectedOptions.includes(option)) {
      setSelectedOptions(selectedOptions.filter((item) => item !== option));
    } else {
      setSelectedOptions([...selectedOptions, option]);
    }
    onSelect?.(option);
  };

  const {
    searchTerm,
    handleSearchChange,
    setSearchTerm, // 전체 선택 후 초기화 등을 위해 가져옴
    activeIndex,
    setActiveIndex,
    filteredOptions,
    handleKeyDown,
  } = useSearchNavigation<T>({
    options,
    filterFn:
      filterFn ||
      ((option, term) =>
        String(option).toLowerCase().includes(term.toLowerCase())),
    onSelect: (option) => {
      toggleOption(option);
    },
    // 백스페이스 눌렀을 때 실행할 액션: 마지막 태그 지우기
    onBackspace: () => {
      if (selectedOptions.length > 0) {
        const newSelection = [...selectedOptions];
        newSelection.pop();
        setSelectedOptions(newSelection);
      }
    },
  });

  // 💡 3. 검색어 변경 래퍼 함수 (onInputChange prop 지원을 위해)
  const onSearchInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    handleSearchChange(value);
    onInputChange?.(value);
  };

  // 컨테이너 클릭 시 인풋 포커스
  const handleContainerClick = () => {
    inputRef.current?.focus();
  };

  const handleOnReset = () => {
    setSelectedOptions([]);
    setSearchTerm('');
  };

  const handleSelectAll = () => {
    const newItemsToSelect = filteredOptions.filter(
      (option) => !selectedOptions.includes(option),
    );

    if (newItemsToSelect.length === 0) return;

    setSelectedOptions([...selectedOptions, ...newItemsToSelect]);

    if (onSelectMultiple) {
      onSelectMultiple(newItemsToSelect);
    } else if (onSelect) {
      newItemsToSelect.forEach((item) => {
        onSelect(item);
      });
    }

    setSearchTerm('');
  };

  const isActive = selectedOptions.length > 0;

  const getLabel = () => {
    if (!isActive) return title;

    const firstOption = selectedOptions[0];

    let firstLabel = String(firstOption);
    if (getDisplayLabel) {
      firstLabel = getDisplayLabel(firstOption);
    } else if (isCategory) {
      firstLabel = CATEGORIES[firstOption as unknown as CategoryId].name;
    }

    if (selectedOptions.length === 1) {
      return `${title}: ${firstLabel}`;
    }
    return `${title}: ${firstLabel} 외 ${selectedOptions.length - 1}건`;
  };

  return (
    <Popover open={isOpen} onOpenChange={setIsOpen} modal={false}>
      <PopoverTrigger asChild>
        <Filter
          size="md"
          isOpen={isOpen}
          active={isActive}
          onReset={handleOnReset}
        >
          {getLabel()}
        </Filter>
      </PopoverTrigger>

      <PopoverContent
        align="start"
        sideOffset={8}
        className="rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex w-75 flex-col"
        onOpenAutoFocus={(e) => {
          e.preventDefault(); // Radix의 기본 포커스 동작(첫 번째 요소 찾기 등)을 막음
          handleContainerClick();
        }}
      >
        {/* --- 인풋 영역 (태그 모드 vs 일반 모드) --- */}
        <div
          className="bg-fill-normal border-line-solid-normal relative flex cursor-text flex-wrap items-center gap-2.5 border-b px-3 py-2"
          onClick={handleContainerClick}
        >
          {/* 1. 태그 렌더링 */}
          {selectedOptions.map((option) => (
            <div key={String(option)} className="shrink-0">
              {isCategory ? (
                <CategoryChip
                  categoryId={option as unknown as CategoryId}
                  onRemove={() => toggleOption(option)}
                />
              ) : (
                <Chip
                  label={
                    getDisplayLabel ? getDisplayLabel(option) : String(option)
                  }
                  onRemove={() => toggleOption(option)}
                />
              )}
            </div>
          ))}

          {/* 2. 실제 인풋  */}
          <input
            ref={inputRef}
            type="text"
            value={searchTerm}
            onChange={onSearchInputChange} // 💡 래퍼 함수로 변경
            onKeyDown={handleKeyDown}
            placeholder={
              selectedOptions.length > 0
                ? ''
                : `하나 이상의 ${title}을/를 입력하세요.`
            }
            className="placeholder:text-label-assistive caption1-medium min-w-0 flex-1 pt-1 outline-none"
            autoComplete="off"
          />
        </div>

        {/* --- 리스트 && footer 영역 --- */}
        <DataTableOptionList
          items={filteredOptions}
          activeIndex={activeIndex}
          setActiveIndex={setActiveIndex}
          isSelected={(option) => selectedOptions.includes(option)}
          onSelect={(option) => toggleOption(option)}
          renderItem={(option) => (
            <div className="text-label-neutral flex-1 truncate text-sm">
              {renderOption(option, searchTerm)}
            </div>
          )}
          customEmptyContent={
            !searchTerm && renderEmptyState ? renderEmptyState() : null
          }
          footer={
            searchTerm && filteredOptions.length > 0 && renderSearchAllTrigger
              ? renderSearchAllTrigger(searchTerm, handleSelectAll)
              : null
          }
        />
        {/* Search All Trigger */}
        {searchTerm &&
          filteredOptions.length > 0 &&
          renderSearchAllTrigger &&
          renderSearchAllTrigger(searchTerm, handleSelectAll)}
      </PopoverContent>
    </Popover>
  );
};

export { DataTableFilterProvider, DataTableSearchFilter };
