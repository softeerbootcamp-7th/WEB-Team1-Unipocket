import TextCellEditor from '@/components/data-table/editors/common/TextCellEditor';
import { useTempInlineExpenseUpdate } from '@/components/data-table/editors/temp-expense/useTempInlineExpenseUpdate.ts';

const TempTextCellEditor = () => {
  const { updateInline } = useTempInlineExpenseUpdate();

  return (
    <TextCellEditor
      onUpdate={(rowId, value) => updateInline(rowId, 'merchantName', value)}
    />
  );
};

export default TempTextCellEditor;
