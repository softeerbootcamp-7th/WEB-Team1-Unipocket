import { Icons } from '@/assets';
import { cn } from '@/lib/utils';

import { useDataTable } from './context';

const Divider = () => <div className="bg-line-solid-normal/30 h-6 w-px" />;

const SelectionActionButton = ({
  children,
  onClick,
  className,
}: React.ComponentProps<'button'>) => {
  return (
    <button onClick={onClick} className={cn('label1-normal-medium', className)}>
      {children}
    </button>
  );
};

const SelectionActionProvider = () => {
  const { table, tableState } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  if (!tableState.selectionMode) return null;

  return (
    <div className="bg-inverse-background text-inverse-label body2-normal-bold fixed bottom-30 left-1/2 w-112.5 -translate-x-1/2 truncate rounded-xl p-3 px-3.5 shadow-2xl">
      <div className="flex items-center gap-4">
        <span className="mr-auto">{selectedRows.length}개 선택됨</span>
        <SelectionActionButton
          onClick={() => {
            /* 카테고리 변경 로직 */
          }}
        >
          카테고리
        </SelectionActionButton>
        <Divider />
        <SelectionActionButton
          onClick={() => {
            /* 카테고리 변경 로직 */
          }}
        >
          결제수단
        </SelectionActionButton>
        <Divider />
        <SelectionActionButton
          onClick={() => {
            /* 카테고리 변경 로직 */
          }}
        >
          여행
        </SelectionActionButton>
        <Divider />
        <SelectionActionButton
          className="text-status-negative"
          onClick={() => {
            /* 삭제 로직 */
          }}
        >
          삭제
        </SelectionActionButton>
        <Divider />
        <Icons.Close height={20} width={20} />
      </div>
    </div>
  );
};

export default SelectionActionProvider;
