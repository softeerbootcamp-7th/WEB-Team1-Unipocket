import { useEffect, useRef, useState } from 'react';

import { useDataTable } from './context';
import type { ActiveCell } from './type';

const DataTableCellEditor = () => {
  const { tableState } = useDataTable();
  const { activeCell } = tableState;

  if (!activeCell) return null;

  return (
    <EditorContent
      key={`${activeCell.rowId}-${activeCell.columnId}`}
      activeCell={activeCell}
    />
  );
};

// 내부용 컴포넌트
const EditorContent = ({ activeCell }: { activeCell: ActiveCell }) => {
  const { dispatch } = useDataTable();
  const [value, setValue] = useState(String(activeCell.value));
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const scrollContainer = document.querySelector('.overflow-y-auto');

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
    // API 호출 및 닫기 로직
    dispatch({ type: 'SET_ACTIVE_CELL', payload: null });
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: activeCell.rect.top,
        left: activeCell.rect.left,
        width: activeCell.rect.width,
        height: activeCell.rect.height,
      }}
      className="rounded-modal-12 shadow-semantic-emphasize z-50 inline-flex bg-white px-2.5 py-3.5"
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

export default DataTableCellEditor;
