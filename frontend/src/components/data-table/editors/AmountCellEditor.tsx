import { useEffect, useRef, useState } from 'react';

import { useDataTable } from '@/components/data-table/context';
import type { ActiveCellState } from '@/components/data-table/type';

const AmountCellEditor = () => {
  const { tableState } = useDataTable();
  const { amountCell } = tableState;

  if (!amountCell) return null;

  return (
    <EditorContent
      key={`${amountCell.rowId}-${amountCell.columnId}`}
      textCell={amountCell}
    />
  );
};

// 내부용 컴포넌트
const EditorContent = ({ textCell }: { textCell: ActiveCellState }) => {
  const { dispatch } = useDataTable();
  const [value, setValue] = useState(String(textCell.value));
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const scrollContainer = document.querySelector(
      '[data-slot="table-container"]',
    );

    if (scrollContainer instanceof HTMLElement) {
      const originalStyle = scrollContainer.style.overflow;
      scrollContainer.style.overflow = 'hidden';

      inputRef.current?.focus();
      inputRef.current?.select();

      return () => {
        scrollContainer.style.overflow = originalStyle;
      };
    }
  }, []);

  const handleSave = () => {
    dispatch({ type: 'SET_AMOUNT_CELL', payload: null });
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: textCell.rect.top,
        left: textCell.rect.left,
        width: textCell.rect.width,
        height: textCell.rect.height,
      }}
      className="rounded-modal-12 shadow-semantic-emphasize z-priority inline-flex bg-white px-2.5 py-3.5"
    >
      <input
        ref={inputRef}
        className="px-2 outline-none"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onBlur={handleSave}
        onKeyDown={(e) => e.key === 'Enter' && handleSave()}
      />
    </div>
  );
};

export default AmountCellEditor;
