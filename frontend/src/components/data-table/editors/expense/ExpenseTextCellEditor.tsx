import TextCellEditor from '@/components/data-table/editors/common/TextCellEditor';

import { useInlineExpenseUpdate } from './useInlineExpenseUpdate';

const ExpenseTextCellEditor = () => {
  const { updateInline } = useInlineExpenseUpdate();

  return (
    <TextCellEditor
      onUpdate={(rowId, value) => updateInline(rowId, 'merchantName', value)}
    />
  );
};

export default ExpenseTextCellEditor;
