import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { ActionPopover } from '@/components/data-table/bars/update/ActionPopover';
import { useDataTable } from '@/components/data-table/context';
import { CategorySelectorContent } from '@/components/data-table/selectors/CategorySelectorContent';

import type { CategoryId } from '@/types/category';

import type { Expense } from '@/api/expenses/type';

interface BulkCategoryActionProps {
  onUpdate: (categoryId: CategoryId) => void;
}

export const BulkCategoryAction = ({ onUpdate }: BulkCategoryActionProps) => {
  const { table } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  const getInitialCategoryId = (): CategoryId | null => {
    if (selectedRows.length === 0) return null;

    const uniqueCategories = new Set(
      selectedRows.map((row) => (row.original as Expense).category),
    );

    return uniqueCategories.size === 1 ? Array.from(uniqueCategories)[0] : null;
  };

  return (
    <ActionPopover
      renderContent={(close) => (
        <CategorySelectorContent
          align="center"
          sideOffset={16}
          initialCategoryId={getInitialCategoryId()}
          onCategorySelect={(val) => {
            onUpdate(val);
            close();
          }}
        />
      )}
    >
      <ActionButton>카테고리</ActionButton>
    </ActionPopover>
  );
};
