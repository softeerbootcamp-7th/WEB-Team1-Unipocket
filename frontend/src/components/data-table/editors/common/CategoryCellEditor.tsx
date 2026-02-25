// components/data-table/editors/shared/CategoryCellEditor.tsx
import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { CategorySelectorContent } from '@/components/data-table/selectors/CategorySelectorContent';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

import type { CategoryId } from '@/types/category';

interface CategoryCellEditorProps {
  onUpdate: (rowId: string, newCategoryId: CategoryId) => void;
}

const CategoryCellEditor = ({ onUpdate }: CategoryCellEditorProps) => {
  const { tableState, dispatch } = useDataTable();
  const { categoryCell } = tableState;

  if (!categoryCell) return null;

  const closeEditor = () =>
    dispatch({ type: 'SET_CATEGORY_CELL', payload: null });

  const handleSelect = (newCategoryId: CategoryId) => {
    if (newCategoryId !== categoryCell.value) {
      onUpdate(categoryCell.rowId, newCategoryId);
    }
    closeEditor();
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && closeEditor()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={categoryCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>
      <CategorySelectorContent
        initialCategoryId={categoryCell.value as CategoryId}
        onCategorySelect={handleSelect}
        onInteractOutside={closeEditor}
      />
    </Popover>
  );
};

export default CategoryCellEditor;
