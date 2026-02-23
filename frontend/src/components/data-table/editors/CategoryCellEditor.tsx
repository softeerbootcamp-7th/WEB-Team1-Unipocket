import { useDataTable } from '@/components/data-table/context';
import { CellEditorAnchor } from '@/components/data-table/editors/CellEditorAnchor';
import { useInlineExpenseUpdate } from '@/components/data-table/editors/useInlineExpenseUpdate';
import { CategorySelectorContent } from '@/components/data-table/selectors/CategorySelectorContent';
import type { ActiveCellState } from '@/components/data-table/type';
import { Popover, PopoverTrigger } from '@/components/ui/popover';

import type { CategoryId } from '@/types/category';

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
  const { updateInline } = useInlineExpenseUpdate();

  const closeEditor = () =>
    dispatch({ type: 'SET_CATEGORY_CELL', payload: null });

  const handleSelect = (newCategoryId: CategoryId) => {
    if (newCategoryId !== categoryCell.value) {
      updateInline(categoryCell.rowId, 'category', newCategoryId);
    }
    closeEditor();
  };

  return (
    <Popover open={true} onOpenChange={(open) => !open && closeEditor()}>
      <PopoverTrigger asChild>
        <CellEditorAnchor rect={categoryCell.rect} style={{ opacity: 0 }} />
      </PopoverTrigger>

      {/* 공통 Content 활용 (align, sideOffset 기본값 사용) */}
      <CategorySelectorContent
        initialCategoryId={categoryCell.value as CategoryId}
        onCategorySelect={handleSelect}
        onInteractOutside={closeEditor}
      />
    </Popover>
  );
};

export default CategoryCellEditor;
