import { useEffect } from 'react';

export function useOutsideClick(
  ref: React.RefObject<HTMLElement | null>,
  callback: () => void,
  options?: {
    ignoreSelector?: string;
  },
) {
  useEffect(() => {
    function handleClick(event: MouseEvent | TouchEvent) {
      const target = event.target as Node;

      if (ref.current && !ref.current.contains(target)) {
        // Check if click is on an ignored element
        if (options?.ignoreSelector && target instanceof Element) {
          if (target.closest(options.ignoreSelector)) {
            return;
          }
        }

        callback();
      }
    }

    document.addEventListener('mousedown', handleClick);
    document.addEventListener('touchstart', handleClick);

    return () => {
      document.removeEventListener('mousedown', handleClick);
      document.removeEventListener('touchstart', handleClick);
    };
  }, [ref, callback, options?.ignoreSelector]);
}
