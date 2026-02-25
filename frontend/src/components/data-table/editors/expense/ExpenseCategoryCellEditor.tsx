import CategoryCellEditor from '@/components/data-table/editors/common/CategoryCellEditor';

import { useInlineExpenseUpdate } from './useInlineExpenseUpdate';

const ExpenseCategoryCellEditor = () => {
  const { updateInline } = useInlineExpenseUpdate();

  return (
    <CategoryCellEditor
      onUpdate={(rowId, val) => updateInline(rowId, 'category', val)}
    />
  );
};

export default ExpenseCategoryCellEditor;
