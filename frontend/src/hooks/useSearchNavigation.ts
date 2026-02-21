import { useState } from 'react';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

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

  const filteredOptions = options.filter((option) =>
    filterFn(option, searchTerm),
  );

  const { activeIndex, setActiveIndex, handleKeyDown } = useKeyboardNavigation({
    items: filteredOptions,
    onSelect: (option) => {
      onSelect(option);
      setSearchTerm(''); // 선택 후 검색어 초기화
    },
    onBackspace,
  });

  const handleSearchChange = (value: string) => {
    setSearchTerm(value);
    setActiveIndex(0); // 검색어가 바뀌면 최상단으로 이동
  };

  return {
    searchTerm,
    setSearchTerm,
    handleSearchChange,
    filteredOptions,
    activeIndex,
    setActiveIndex,
    handleKeyDown: (e: React.KeyboardEvent<HTMLInputElement>) =>
      handleKeyDown(e, searchTerm === ''), // 현재 검색어 상태 전달
  };
};
