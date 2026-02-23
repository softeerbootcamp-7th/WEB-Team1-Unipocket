import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';

import SelectionActionBar from '@/components/data-table/bars/SelectionActionBar';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import TravelCellEditor from '@/components/data-table/editors/TravelCellEditor';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';
import TableSidePanel from '@/components/side-panel/TableSidePanel';
import UploadMenu from '@/components/upload/UploadMenu';

const ExpenseTable = () => {
  const { data, totalPages, filter, updateFilter } = useFilteredExpenses();

  return (
    <div className="bg-background-normal relative flex min-h-0 flex-1 flex-col px-2 pt-4">
      <BaseExpenseTable
        data={data}
        filter={filter}
        totalPages={totalPages}
        updateFilter={updateFilter}
        filterActions={<UploadMenu />}
      >
        <SelectionActionBar />
        <TextCellEditor />
        <CategoryCellEditor />
        <TravelCellEditor />
        <TableSidePanel />
      </BaseExpenseTable>
    </div>
  );
};

export default ExpenseTable;
