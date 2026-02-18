import { useDataTable } from '@/components/data-table/context';
import SidePanelUI from '@/components/side-panel/SidePanelUI';

import type { ExpenseResponse } from '@/api/expenses/type';

const TableSidePanel = () => {
  const { tableState, dispatch } = useDataTable();
  const { activeRow } = tableState;

  return (
    <SidePanelUI
      isOpen={!!activeRow}
      onClose={() => dispatch({ type: 'SET_ACTIVE_ROW', payload: null })}
      initialData={activeRow?.value as ExpenseResponse}
    />
  );
};

export default TableSidePanel;
