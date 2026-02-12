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

  const addRecentSearch = useCallback(
    (termOrTerms: string | string[]) => {
      setRecentSearches((prev) => {
        const newTerms = Array.isArray(termOrTerms)
          ? termOrTerms
          : [termOrTerms];
        if (newTerms.length === 0) return prev;

        const combined = [...newTerms, ...prev];
        const uniqueTerms = Array.from(new Set(combined)).filter(
          (t) => t.trim() !== '',
        );
        const updated = uniqueTerms.slice(0, maxItems);

        localStorage.setItem(storageKey, JSON.stringify(updated));

        return updated;
      });
    },
    [storageKey, maxItems],
  );

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
