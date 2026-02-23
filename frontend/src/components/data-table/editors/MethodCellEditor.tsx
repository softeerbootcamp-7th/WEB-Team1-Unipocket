import { useState } from 'react';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

import Chip from '@/components/common/Chip';
import { useDataTable } from '@/components/data-table/context';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import type { ActiveCellState } from '@/components/data-table/type';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { useGetCardsQuery } from '@/api/users/query';

const MethodCellEditor = () => {
  const { tableState } = useDataTable();
  const { methodCell } = tableState;

  if (!methodCell) return null;

  return (
    <MethodCellEditorContent
      key={`${methodCell.rowId}-${methodCell.columnId}`}
      methodCell={methodCell}
    />
  );
};

const MethodCellEditorContent = ({
  methodCell,
}: {
  methodCell: ActiveCellState;
}) => {
  const { dispatch } = useDataTable();
  const [selectedMethod, setSelectedMethod] = useState<string>(
    methodCell.value as string,
  );

  const { data: cards } = useGetCardsQuery();
  const options = cards.map((card) => card.cardNumber);

  const getNickName = (cardNumber: string) => {
    const card = cards.find((c) => c.cardNumber === cardNumber);
    return card?.nickName || cardNumber;
  };

  const initialIndex = Math.max(0, options.indexOf(selectedMethod));

  const { activeIndex, setActiveIndex, handleKeyDown } =
    useKeyboardNavigation<string>({
      items: options,
      initialActiveIndex: initialIndex,
      onSelect: (selectedId) => {
        setSelectedMethod(selectedId);
        handleSave();
      },
    });

  const handleSave = () => {
    dispatch({ type: 'SET_METHOD_CELL', payload: null });
  };

  const handleSelectAndSave = (selectedId: string) => {
    setSelectedMethod(selectedId);
    dispatch({ type: 'SET_METHOD_CELL', payload: null });
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && handleSave()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={methodCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>

      <PopoverContent
        align="start"
        sideOffset={0}
        className="rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex w-75 flex-col p-0 outline-none"
        onInteractOutside={() => handleSave()}
        onKeyDown={handleKeyDown}
      >
        <DataTableOptionList
          items={options}
          activeIndex={activeIndex}
          setActiveIndex={setActiveIndex}
          isSelected={(item) => selectedMethod === item}
          onSelect={handleSelectAndSave}
          renderItem={(item) => <Chip label={getNickName(item)} />}
        />
      </PopoverContent>
    </Popover>
  );
};

export default MethodCellEditor;
