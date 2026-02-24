import ImportToFolderBar from '@/components/data-table/bars/import/ImportToFolderBar';
import { expenseColumns } from '@/components/data-table/columns/expenseColumns';
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
      columns={expenseColumns}
    >
      <ImportToFolderBar />
    </BaseExpenseTable>
  );
};

export default ImportExpenseTable;
