import { useState } from 'react';

interface UseSearchNavigationProps<T> {
  options: T[];
  filterFn: (option: T, searchTerm: string) => boolean;
  onSelect: (option: T) => void;
  onBackspace?: () => void; // 검색어가 없을 때 백스페이스 누를 경우
}

export const useSearchNavigation = <T>({
  options,
  filterFn,
  onSelect,
  onBackspace,
}: UseSearchNavigationProps<T>) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [activeIndex, setActiveIndex] = useState(0);

  // 1. 옵션 필터링
  const filteredOptions = options.filter((option) =>
    filterFn(option, searchTerm),
  );

  // 2. 검색어 변경 핸들러 (인덱스 초기화 포함)
  const handleSearchChange = (value: string) => {
    setSearchTerm(value);
    setActiveIndex(0);
  };

  // 3. 키보드 네비게이션 로직
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && searchTerm === '') {
      onBackspace?.();
      return;
    }

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex((prev) =>
        prev + 1 >= filteredOptions.length ? 0 : prev + 1,
      );
      return;
    }

    if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex((prev) =>
        prev - 1 < 0 ? filteredOptions.length - 1 : prev - 1,
      );
      return;
    }

    if (e.key === 'Enter') {
      if (e.nativeEvent.isComposing) return;
      e.preventDefault();

      const targetOption = filteredOptions[activeIndex];
      if (targetOption) {
        onSelect(targetOption);
        setSearchTerm(''); // 선택 후 검색어 초기화
      }
    }
  };

  return {
    searchTerm,
    handleSearchChange,
    setSearchTerm,
    activeIndex,
    setActiveIndex,
    filteredOptions,
    handleKeyDown,
  };
};
