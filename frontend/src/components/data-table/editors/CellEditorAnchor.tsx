import type { ComponentPropsWithoutRef } from 'react';
import { forwardRef, useEffect } from 'react';

import type { ActiveCellState } from '@/components/data-table/type';

interface CellEditorAnchorProps extends ComponentPropsWithoutRef<'div'> {
  rect: ActiveCellState['rect'];
}

export const CellEditorAnchor = forwardRef<
  HTMLDivElement,
  CellEditorAnchorProps
>(({ rect, style, children, ...props }, ref) => {
  useEffect(() => {
    const scrollContainer = document.querySelector(
      '[data-slot="table-container"]',
    );

    if (scrollContainer instanceof HTMLElement) {
      const originalStyle = scrollContainer.style.overflow;
      scrollContainer.style.overflow = 'hidden';

      return () => {
        scrollContainer.style.overflow = originalStyle;
      };
    }
  }, []);

  return (
    <div
      ref={ref}
      style={{
        position: 'fixed',
        top: rect.top,
        left: rect.left,
        width: rect.width,
        height: rect.height,
        ...style,
      }}
      {...props}
    >
      {children}
    </div>
  );
});

CellEditorAnchor.displayName = 'CellEditorAnchor';
