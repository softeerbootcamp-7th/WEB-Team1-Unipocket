import { useEffect, useMemo, useRef, useState } from 'react';

import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import type { ActiveCellState } from '@/components/data-table/type';

import type { Expense } from '@/api/expenses/type';

const AmountCellEditor = () => {
  const { tableState } = useDataTable();
  const { amountCell } = tableState;

  if (!amountCell) return null;

  return (
    <AmountCellEditorContent
      key={`${amountCell.rowId}-${amountCell.columnId}`}
      amountCell={amountCell}
    />
  );
};

const AmountCellEditorContent = ({
  amountCell,
}: {
  amountCell: ActiveCellState;
}) => {
  const { table, dispatch } = useDataTable();
  const [value, setValue] = useState(String(amountCell.value));
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    inputRef.current?.focus();
    inputRef.current?.select();
  }, []);

  const handleSave = () => {
    dispatch({ type: 'SET_AMOUNT_CELL', payload: null });
  };

  const currencySymbol = useMemo(() => {
    try {
      const row = table.getRow(amountCell.rowId);
      const original = row.original as Expense;

      const currencyCode =
        amountCell.columnId === 'baseCurrencyAmount'
          ? original.baseCurrencyCode
          : original.localCurrencyCode;

      const parts = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: currencyCode,
        currencyDisplay: 'narrowSymbol',
      }).formatToParts(0);

      return parts.find((part) => part.type === 'currency')?.value || '$';
    } catch {
      return '$'; // fallback
    }
  }, [table, amountCell.rowId, amountCell.columnId]);

  return (
    <CellEditorAnchor
      rect={amountCell.rect}
      className="rounded-modal-12 shadow-semantic-emphasize z-priority inline-flex items-center gap-1 bg-white px-2.5 py-3.5"
    >
      <span className="text-sm text-gray-400 select-none">
        {currencySymbol}
      </span>
      <input
        ref={inputRef}
        className="w-full px-1 outline-none"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onBlur={handleSave}
        onKeyDown={(e) => e.key === 'Enter' && handleSave()}
      />
    </CellEditorAnchor>
  );
};

export default AmountCellEditor;
