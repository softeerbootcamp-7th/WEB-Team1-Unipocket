import { useState } from 'react';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

import { CategoryChip } from '@/components/common/Chip';
import { useDataTable } from '@/components/data-table/context';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import type { ActiveCellState } from '@/components/data-table/type';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { CATEGORIES, type CategoryId } from '@/types/category';

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
  const [selectedCategoryId, setSelectedCategoryId] = useState<CategoryId>(
    categoryCell.value as CategoryId,
  );

  const options = Object.keys(CATEGORIES) as unknown as CategoryId[];

  const { activeIndex, setActiveIndex, handleKeyDown } =
    useKeyboardNavigation<CategoryId>({
      items: options,
      initialActiveIndex: selectedCategoryId,
      onSelect: (selectedId) => {
        setSelectedCategoryId(selectedId);
        handleSave();
      },
    });

  const handleSave = () => {
    dispatch({ type: 'SET_CATEGORY_CELL', payload: null });
  };

  const handleSelectAndSave = (selectedId: CategoryId) => {
    setSelectedCategoryId(selectedId);
    dispatch({ type: 'SET_CATEGORY_CELL', payload: null });
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && handleSave()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={categoryCell.rect} style={{ opacity: 0 }} />
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
          isSelected={(item) => Number(selectedCategoryId) === Number(item)}
          onSelect={handleSelectAndSave}
          renderItem={(item) => <CategoryChip categoryId={item} />}
        />
      </PopoverContent>
    </Popover>
  );
};

export default CategoryCellEditor;
