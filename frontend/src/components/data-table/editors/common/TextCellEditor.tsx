import { useEffect, useRef, useState } from 'react';

import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import type { ActiveCellState } from '@/components/data-table/type';

interface TextCellEditorProps {
  onUpdate: (rowId: string, value: string) => void;
}

const TextCellEditor = ({ onUpdate }: TextCellEditorProps) => {
  const { tableState } = useDataTable();
  const { textCell } = tableState;

  if (!textCell) return null;

  return (
    <TextCellEditorContent
      key={`${textCell.rowId}-${textCell.columnId}`}
      textCell={textCell}
      onUpdate={onUpdate}
    />
  );
};

const TextCellEditorContent = ({
  textCell,
  onUpdate,
}: {
  textCell: ActiveCellState;
} & TextCellEditorProps) => {
  const { dispatch } = useDataTable();
  // null 대비 안전한 초기값 설정
  const [value, setValue] = useState(String(textCell.value || ''));
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    inputRef.current?.focus();
    inputRef.current?.select();
  }, []);

  const handleSave = () => {
    if (value.trim() !== '' && value !== String(textCell.value || '')) {
      onUpdate(textCell.rowId, value);
    }
    dispatch({ type: 'SET_TEXT_CELL', payload: null });
  };

  return (
    <CellEditorAnchor
      rect={textCell.rect}
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
    </CellEditorAnchor>
  );
};

export default TextCellEditor;
