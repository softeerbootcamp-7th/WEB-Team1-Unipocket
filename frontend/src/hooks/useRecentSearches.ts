import { useCallback, useState } from 'react';

export const useRecentSearches = (storageKey: string, maxItems = 5) => {
  const [recentSearches, setRecentSearches] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // useCallback 적용: 함수 재생성 방지
  const addRecentSearch = useCallback(
    (termOrTerms: string | string[]) => {
      setRecentSearches((prev) => {
        const newTerms = Array.isArray(termOrTerms)
          ? termOrTerms
          : [termOrTerms];
        if (newTerms.length === 0) return prev;

        // 이전 상태(prev)를 기반으로 계산
        const combined = [...newTerms, ...prev];
        const uniqueTerms = Array.from(new Set(combined)).filter(
          (t) => t.trim() !== '',
        );
        const updated = uniqueTerms.slice(0, maxItems);

        // 로컬 스토리지 저장은 사이드 이펙트이므로 여기서 수행해도 되지만,
        // 더 엄격하게 하려면 useEffect로 빼는 것이 정석입니다.
        // 편의상 상태 업데이트 시점에 같이 수행합니다.
        localStorage.setItem(storageKey, JSON.stringify(updated));

        return updated;
      });
    },
    [storageKey, maxItems],
  ); // recentSearches 의존성 제거

  // useCallback 적용
  const removeRecentSearch = useCallback(
    (keyword: string) => {
      setRecentSearches((prev) => {
        const updated = prev.filter((item) => item !== keyword);
        localStorage.setItem(storageKey, JSON.stringify(updated));
        return updated;
      });
    },
    [storageKey],
  );

  return { recentSearches, addRecentSearch, removeRecentSearch };
};
