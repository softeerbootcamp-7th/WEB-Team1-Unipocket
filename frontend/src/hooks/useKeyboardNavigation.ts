import { useState } from 'react';

interface UseKeyboardNavigationProps<T> {
  items: T[];
  onSelect: (option: T) => void;
  onBackspace?: () => void; // 검색어가 없을 때 백스페이스 누를 경우
  initialActiveIndex?: number; // 초기 활성 인덱스 (기본값: 0)
}

export const useKeyboardNavigation = <T>({
  items,
  onSelect,
  onBackspace,
  initialActiveIndex = 0,
}: UseKeyboardNavigationProps<T>) => {
  const [activeIndex, setActiveIndex] = useState(initialActiveIndex);

  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLElement>,
    isEmptySearch: boolean = false,
  ) => {
    if (e.key === 'Escape') {
      return;
    }
    if (e.key === 'Backspace' && isEmptySearch) {
      onBackspace?.();
      return;
    }
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex((prev) => (prev + 1 >= items.length ? 0 : prev + 1));
      return;
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex((prev) => (prev - 1 < 0 ? items.length - 1 : prev - 1));
      return;
    }
    if (e.key === 'Enter') {
      if (e.nativeEvent.isComposing) return;
      e.preventDefault();
      const targetOption = items[activeIndex];
      if (targetOption) onSelect(targetOption);
    }
  };

  return { activeIndex, setActiveIndex, handleKeyDown };
};
