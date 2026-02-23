import { useFilteredExpenses } from '@/hooks/useFilteredExpenses';

import ImportToFolderBar from '@/components/data-table/bars/ImportToFolderBar';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

const ImportExpenseTable = () => {
  const { data, filter, updateFilter } = useFilteredExpenses();

  return (
    <BaseExpenseTable
      data={data}
      filter={filter}
      updateFilter={updateFilter}
      blankFallbackText="여행 지출 내역을 추가해주세요"
    >
      <ImportToFolderBar />
    </BaseExpenseTable>
  );
};

export default ImportExpenseTable;
