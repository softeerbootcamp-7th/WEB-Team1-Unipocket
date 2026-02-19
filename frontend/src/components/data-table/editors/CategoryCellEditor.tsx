import { useEffect, useRef, useState } from 'react';

import { useDataTable } from '@/components/data-table/context';
import type { ActiveCellState } from '@/components/data-table/type';

const CategoryCellEditor = () => {
  const { tableState } = useDataTable();
  const { categoryCell } = tableState;

  if (!categoryCell) return null;

  return (
    <CategoryCellEditorContent
      key={`${categoryCell.rowId}-${categoryCell.columnId}`}
      categoryCell={categoryCell}
    />
  );
};

const CategoryCellEditorContent = ({
  categoryCell,
}: {
  categoryCell: ActiveCellState;
}) => {
  const { dispatch } = useDataTable();
  const [value, setValue] = useState(String(categoryCell.value));
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
    dispatch({ type: 'SET_TEXT_CELL', payload: null });
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: categoryCell.rect.top,
        left: categoryCell.rect.left,
        width: categoryCell.rect.width,
        height: categoryCell.rect.height,
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

export default CategoryCellEditor;
