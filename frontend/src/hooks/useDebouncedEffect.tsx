import { useEffect, useRef } from 'react';

export const useDebouncedEffect = (
  effect: () => void | (() => void),
  deps: React.DependencyList,
  delay = 500,
) => {
  const cleanupRef = useRef<ReturnType<typeof effect> | undefined>(undefined);

  useEffect(() => {
    const timer = setTimeout(() => {
      cleanupRef.current = effect();
    }, delay);

    return () => {
      clearTimeout(timer);
      if (typeof cleanupRef.current === 'function') {
        cleanupRef.current();
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, delay]);
};
