import { useEffect, useMemo, useRef, useState } from 'react';

import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import type { ActiveCellState } from '@/components/data-table/type';

import { REGEX } from '@/constants/regex';
import type { CurrencyCode } from '@/data/country/currencyCode';
import { getLocaleByCurrencyCode } from '@/lib/country';

interface AmountCellEditorProps<TData> {
  onUpdate: (
    rowId: string,
    field: 'localCurrencyAmount' | 'baseCurrencyAmount',
    value: number,
    oppositeField: 'localCurrencyAmount' | 'baseCurrencyAmount',
  ) => void;
  getCurrencyCode: (original: TData, isLocal: boolean) => CurrencyCode | null;
}

const AmountCellEditor = <TData,>({
  onUpdate,
  getCurrencyCode,
}: AmountCellEditorProps<TData>) => {
  const { tableState } = useDataTable();
  const { amountCell } = tableState;

  if (!amountCell) return null;

  return (
    <AmountCellEditorContent
      key={`${amountCell.rowId}-${amountCell.columnId}`}
      amountCell={amountCell}
      onUpdate={onUpdate}
      getCurrencyCode={getCurrencyCode}
    />
  );
};

const AmountCellEditorContent = <TData,>({
  amountCell,
  onUpdate,
  getCurrencyCode,
}: {
  amountCell: ActiveCellState;
} & AmountCellEditorProps<TData>) => {
  const { table, dispatch } = useDataTable();
  const [value, setValue] = useState(String(amountCell.value || ''));
  const inputRef = useRef<HTMLInputElement>(null);

  const isCancelingRef = useRef(false);

  useEffect(() => {
    inputRef.current?.focus();
    inputRef.current?.select();
  }, []);

  const handleCancel = () => {
    isCancelingRef.current = true;
    dispatch({ type: 'SET_AMOUNT_CELL', payload: null });
  };

  const handleSave = () => {
    if (isCancelingRef.current) return;

    if (value !== String(amountCell.value || '')) {
      const isLocal = amountCell.columnId === 'localCurrencyAmount';
      const field = isLocal ? 'localCurrencyAmount' : 'baseCurrencyAmount';
      const oppositeField = isLocal
        ? 'baseCurrencyAmount'
        : 'localCurrencyAmount';

      onUpdate(amountCell.rowId, field, Number(value), oppositeField);
    }
    dispatch({ type: 'SET_AMOUNT_CELL', payload: null });
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSave();
    if (e.key === 'Escape') handleCancel();
  };

  const currencySymbol = useMemo(() => {
    try {
      const row = table.getRow(amountCell.rowId);
      const isLocal = amountCell.columnId === 'localCurrencyAmount';

      const currencyCode = getCurrencyCode(row.original as TData, isLocal);

      if (!currencyCode) return '$';

      const locale = getLocaleByCurrencyCode(currencyCode);
      const parts = new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: currencyCode,
        currencyDisplay: 'narrowSymbol',
      }).formatToParts(0);

      return parts.find((part) => part.type === 'currency')?.value || '$';
    } catch {
      return '$';
    }
  }, [table, amountCell.rowId, amountCell.columnId, getCurrencyCode]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value.replace(REGEX.NON_NUMERIC, ''));
  };

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
        onChange={handleChange}
        onBlur={handleSave}
        onKeyDown={handleKeyDown}
      />
    </CellEditorAnchor>
  );
};

export default AmountCellEditor;
