import type { Row, Table } from '@tanstack/react-table';

import type { TableUIAction } from '@/components/data-table/type';

import { cn } from '@/lib/utils';

interface SidePanelButtonProps<TData> {
  row: Row<TData>;
  table: Table<TData>;
}

const SidePanelButton = <TData,>({
  row,
  table,
}: SidePanelButtonProps<TData>) => {
  const handleOpenSidePanel = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    e.currentTarget.blur();

    const meta = table.options.meta as {
      dispatch: React.Dispatch<TableUIAction>;
    };

    meta?.dispatch({
      type: 'SET_ACTIVE_ROW',
      payload: {
        rowId: row.id,
        value: row.original,
      },
    });
  };

  return (
    <button
      onClick={handleOpenSidePanel}
      className={cn(
        'rounded-modal-6 shadow-semantic-emphasize bg-background-normal px-1.25 py-1',
        'label2-medium text-label-neutral',
        'invisible cursor-pointer group-hover/row:visible focus:visible',
      )}
    >
      열기
    </button>
  );
};

export default SidePanelButton;
