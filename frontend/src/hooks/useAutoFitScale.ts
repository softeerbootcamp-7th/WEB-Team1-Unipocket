import { useLayoutEffect, useRef, useState } from 'react';

/**
 * 콘텐츠가 지정된 너비(maxWidth)를 초과할 때 자동으로 축소(scale) 비율을 계산하는 훅
 *
 * @param maxWidth - 콘텐츠가 넘어가지 않아야 할 최대 너비 (px)
 * @param dependencies - 크기 재계산이 필요한 의존성 배열 (예: [text, amount])
 * @returns {{ ref: React.RefObject<T>, scale: number }} ref를 타겟 요소에 연결하고, scale을 style.transform에 적용
 *
 * @example
 * const { ref, scale } = useAutoFitScale(100, [text]);
 * return <div ref={ref} style={{ transform: `scale(${scale})` }}>{text}</div>
 */
export const useAutoFitScale = <T extends HTMLElement>(
  maxWidth: number,
  dependencies: unknown[] = [],
) => {
  const ref = useRef<T>(null);
  const [scale, setScale] = useState(1);

  useLayoutEffect(() => {
    if (ref.current) {
      const { scrollWidth } = ref.current;
      if (scrollWidth > maxWidth) {
        setScale(maxWidth / scrollWidth);
      } else {
        setScale(1);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxWidth, ...dependencies]);

  return { ref, scale };
};
