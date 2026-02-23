import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { ActionPopover } from '@/components/data-table/bars/update/ActionPopover';
import { CategorySelectorContent } from '@/components/data-table/selectors/CategorySelectorContent';

import type { CategoryId } from '@/types/category';

interface BulkCategoryActionProps {
  onUpdate: (categoryId: CategoryId) => void;
}

export const BulkCategoryAction = ({ onUpdate }: BulkCategoryActionProps) => {
  return (
    <ActionPopover
      renderContent={(close) => (
        <CategorySelectorContent
          align="center"
          sideOffset={16}
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
