import CategoryCellEditor from '@/components/data-table/editors/common/CategoryCellEditor';
import { useTempInlineExpenseUpdate } from '@/components/data-table/editors/temp-expense/useTempInlineExpenseUpdate.ts';

const TempCategoryCellEditor = () => {
  const { updateInline } = useTempInlineExpenseUpdate();

  return (
    <CategoryCellEditor
      onUpdate={(rowId, val) => updateInline(rowId, 'category', val)}
    />
  );
};

export default TempCategoryCellEditor;
