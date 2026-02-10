import {
  type ComponentPropsWithoutRef,
  useMemo,
  useRef,
  useState,
} from 'react';
import { clsx } from 'clsx';

import { Checkbox } from '@/components/ui/checkbox';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import Filter from '../Filter';

const DataTableFilterProvider = ({
  children,
}: ComponentPropsWithoutRef<'div'>) => {
  return <div className="flex items-center gap-3 px-2.5">{children}</div>;
};

interface DataTableSearchFilterProps<T extends string> {
  title: string;
  options: T[];
  selectedOptions: T[];
  setSelectedOptions: (selected: T[]) => void;
  // 렌더링 옵션
  renderOption: (option: T, searchTerm: string) => React.ReactNode;
  renderTag?: (option: T, onRemove: () => void) => React.ReactNode; // 태그 렌더링 함수 (있으면 태그 인풋 모드)
  renderEmptyState?: () => React.ReactNode;
  renderFooter?: (searchTerm: string) => React.ReactNode;
  onSearch?: (term: string) => void;
}

const DataTableSearchFilter = <T extends string>({
  title,
  options,
  selectedOptions,
  setSelectedOptions,
  renderOption,
  renderTag,
  renderEmptyState,
  renderFooter,
  onSearch,
}: DataTableSearchFilterProps<T>) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeIndex, setActiveIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  // 검색 로직
  const filteredOptions = useMemo(() => {
    if (!searchTerm) return options;
    return options.filter((option) =>
      option.toLowerCase().includes(searchTerm.toLowerCase()),
    );
  }, [options, searchTerm]);

  // 검색어 변경
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);
    setActiveIndex(0);
    // api 호출예정
    onSearch?.(value);
  };

  // 항목 선택/해제 토글
  const toggleOption = (option: T) => {
    if (selectedOptions.includes(option)) {
      setSelectedOptions(selectedOptions.filter((item) => item !== option));
    } else {
      setSelectedOptions([...selectedOptions, option]);
    }
    setSearchTerm('');
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    // 백스페이스로 마지막 태그 삭제
    if (
      e.key === 'Backspace' &&
      searchTerm === '' &&
      selectedOptions.length > 0 &&
      renderTag
    ) {
      const newSelection = [...selectedOptions];
      newSelection.pop(); // 마지막 항목 제거
      setSelectedOptions(newSelection);
    }

    // 2. 화살표 아래 (다음 항목)
    if (e.key === 'ArrowDown') {
      e.preventDefault(); // 커서 이동 방지
      setActiveIndex((prev) =>
        prev + 1 >= filteredOptions.length ? 0 : prev + 1,
      );
      return;
    }

    // 3. 화살표 위 (이전 항목)
    if (e.key === 'ArrowUp') {
      e.preventDefault(); // 커서 이동 방지
      setActiveIndex((prev) =>
        prev - 1 < 0 ? filteredOptions.length - 1 : prev - 1,
      );
      return;
    }

    // 4. 엔터 (현재 하이라이트된 항목 선택)
    if (e.key === 'Enter') {
      if (e.nativeEvent.isComposing) return;
      e.preventDefault();
      // 현재 하이라이트된 옵션 가져오기
      const targetOption = filteredOptions[activeIndex];

      if (targetOption) {
        toggleOption(targetOption);
      }
    }
  };

  // 컨테이너 클릭 시 인풋 포커스
  const handleContainerClick = () => {
    inputRef.current?.focus();
  };

  const handleOnReset = () => {
    setSelectedOptions([]);
    setSearchTerm('');
  };

  const isActive = selectedOptions.length > 0;

  const getLabel = () => {
    if (!isActive) return title;
    if (selectedOptions.length === 1) return `${title}: ${selectedOptions[0]}`;
    return `${title}: ${selectedOptions[0]} 외 ${selectedOptions.length - 1}건`;
  };

  return (
    <Popover open={isOpen} onOpenChange={setIsOpen}>
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
        className="rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex min-w-75 flex-col overflow-hidden"
        onOpenAutoFocus={(e) => e.preventDefault()} // 팝오버 열릴 때 자동 포커스 방지 (인풋 제어 위해)
      >
        {/* --- 인풋 영역 (태그 모드 vs 일반 모드) --- */}
        <div
          className="bg-fill-normal border-line-solid-normal relative flex cursor-text items-center gap-2.5 border-b px-3 py-2"
          onClick={handleContainerClick}
        >
          {/* 1. 태그 렌더링 (renderTag가 있을 때만) */}
          {renderTag &&
            selectedOptions.map((option) => (
              <div key={option} className="shrink-0">
                {renderTag(option, () => toggleOption(option))}
              </div>
            ))}

          {/* 2. 실제 인풋  */}
          <input
            ref={inputRef}
            type="text"
            value={searchTerm}
            onChange={handleSearchChange}
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
        <div ref={listRef} className="max-h-85 overflow-y-auto p-3">
          {!searchTerm && renderEmptyState ? (
            renderEmptyState()
          ) : (
            <>
              {filteredOptions.length > 0 ? (
                filteredOptions.map((option, index) => (
                  <label
                    key={option}
                    onMouseEnter={() => setActiveIndex(index)}
                    className={clsx(
                      'group rounded-modal-6 flex cursor-pointer items-center gap-2.5 px-3 py-2 transition-colors',
                      activeIndex === index && 'bg-background-alternative',
                    )}
                  >
                    <Checkbox
                      checked={selectedOptions.includes(option)}
                      onCheckedChange={() => toggleOption(option)}
                    />

                    {/* 옵션 텍스트/컴포넌트 */}
                    <div className="text-label-neutral flex-1 truncate text-sm">
                      {renderOption(option, searchTerm)}
                    </div>
                  </label>
                ))
              ) : (
                <div className="text-label-assistive p-4 text-center text-sm">
                  검색 결과가 없습니다.
                </div>
              )}
            </>
          )}
          {/* Footer */}
          {searchTerm && renderFooter && renderFooter(searchTerm)}
        </div>
      </PopoverContent>
    </Popover>
  );
};

export { DataTableFilterProvider, DataTableSearchFilter };
