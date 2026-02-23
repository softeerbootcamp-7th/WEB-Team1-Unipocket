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

/**
 * 콘텐츠가 뷰포트 높이를 초과할 때 자동으로 축소(scale) 비율을 계산하는 훅
 * transform: scale()은 레이아웃에 영향을 주지 않으므로 scrollHeight로 원본 크기를 측정
 *
 * @param paddingBottom - 뷰포트 하단 여유 공간 (px, 기본값 16)
 * @param minScale - 축소 가능한 최솟값 (0~1, 기본값 0 = 제한 없음)
 * @param dependencies - 크기 재계산이 필요한 의존성 배열
 * @returns {{ ref, scale }} ref를 타겟 요소에 연결하고, scale을 style.transform에 적용
 *
 * @example
 * const { ref, scale } = useAutoFitScaleToViewport(16, 0.7, [data]);
 * return (
 *   <div ref={ref} style={{ transform: `scale(${scale})`, transformOrigin: 'top left' }}>
 *     {children}
 *   </div>
 * )
 */
export const useAutoFitScaleToViewport = <T extends HTMLElement>(
  paddingBottom: number = 16,
  minScale: number = 0,
  dependencies: unknown[] = [],
) => {
  const ref = useRef<T>(null);
  const [scale, setScale] = useState(1);

  useLayoutEffect(() => {
    const el = ref.current;
    if (!el) return;

    // getBoundingClientRect().top은 transform-origin: top 기준에서 scale 후에도 동일
    const rect = el.getBoundingClientRect();
    const availableHeight = window.innerHeight - rect.top - paddingBottom;

    // scrollHeight는 CSS transform에 영향받지 않아 항상 원본 높이를 반환
    const naturalHeight = el.scrollHeight;

    if (naturalHeight > availableHeight && availableHeight > 0) {
      setScale(Math.max(minScale, availableHeight / naturalHeight));
    } else {
      setScale(1);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [paddingBottom, minScale, ...dependencies]);

  return { ref, scale };
};
