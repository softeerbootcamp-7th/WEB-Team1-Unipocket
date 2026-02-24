import UpdateActionBar from '@/components/data-table/bars/update/UpdateActionBar';
import AmountCellEditor from '@/components/data-table/editors/AmountCellEditor';
import CategoryCellEditor from '@/components/data-table/editors/CategoryCellEditor';
import MethodCellEditor from '@/components/data-table/editors/MethodCellEditor';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
import TravelCellEditor from '@/components/data-table/editors/TravelCellEditor';
import { useFilteredExpenses } from '@/components/data-table/filters/useFilteredExpenses';
import BaseExpenseTable from '@/components/expense/BaseExpenseTable';
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
        <UpdateActionBar />
        <TextCellEditor />
        <CategoryCellEditor />
        <AmountCellEditor />
        <MethodCellEditor />
        <TravelCellEditor />
      </BaseExpenseTable>
    </div>
  );
};

export default ExpenseTable;
