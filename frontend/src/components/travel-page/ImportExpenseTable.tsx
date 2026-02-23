import ImportToFolderBar from '@/components/data-table/bars/ImportToFolderBar';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';

const ImportExpenseTable = () => {
  const { data, filter, updateFilter, totalPages } = useFilteredExpenses();

  return (
    <BaseExpenseTable
      data={data}
      filter={filter}
      updateFilter={updateFilter}
      blankFallbackText="여행 지출 내역을 추가해주세요"
      totalPages={totalPages}
    >
      <ImportToFolderBar />
    </BaseExpenseTable>
  );
};

export default ImportExpenseTable;
