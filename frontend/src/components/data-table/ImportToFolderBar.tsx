import { useDataTable } from '@/components/data-table/context';

import { Icons } from '@/assets';

const Divider = () => <div className="bg-line-solid-normal/30 h-6 w-px" />;

const ImportToFolderBar = () => {
  const { table, tableState } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  if (!tableState.selectionMode) return null;

  const handleImportToFolder = () => {};

  return (
    <div className="bg-background-normal body2-normal-bold rounded-modal-12 shadow-semantic-strong fixed bottom-30 left-1/2 w-125 -translate-x-1/2 truncate px-4 py-2.75">
      <div className="flex items-center gap-4">
        <span className="mr-auto">{selectedRows.length}개 선택됨</span>
        <button
          onClick={handleImportToFolder}
          className="label1-normal-medium text-primary-normal"
        >
          현재 폴더에 추가하기
        </button>
        <Divider />
        <Icons.Close height={20} width={20} />
      </div>
    </div>
  );
};

export default ImportToFolderBar;
