import { useEffect } from 'react';

export const useDebouncedEffect = (
  effect: () => void | (() => void),
  deps: React.DependencyList,
  delay = 500,
) => {
  useEffect(() => {
    let cleanup: void | (() => void);
    const timer = setTimeout(() => {
      cleanup = effect();
    }, delay);

    return () => {
      clearTimeout(timer);
      if (typeof cleanup === 'function') {
        cleanup();
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, delay]);
};