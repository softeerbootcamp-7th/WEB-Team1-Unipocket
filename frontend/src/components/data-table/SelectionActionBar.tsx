import React from 'react';

import { useDataTable } from '@/components/data-table/context';

import { Icons } from '@/assets';
import { cn } from '@/lib/utils';

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

const SelectionActionBar = () => {
  const { table, tableState } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  const actionButtons = [
    { label: '카테고리', onClick: () => console.log('카테고리 변경') },
    { label: '결제수단', onClick: () => console.log('결제수단 변경') },
    { label: '여행', onClick: () => console.log('여행 변경') },
    {
      label: '삭제',
      onClick: () => console.log('삭제 실행'),
      className: 'text-status-negative',
    },
  ];

  if (!tableState.selectionMode) return null;

  return (
    <div className="bg-inverse-background text-inverse-label body2-normal-bold fixed bottom-30 left-1/2 w-112.5 -translate-x-1/2 truncate rounded-xl p-3 px-3.5 shadow-2xl">
      <div className="flex items-center gap-4">
        <span className="mr-auto">{selectedRows.length}개 선택됨</span>
        {actionButtons.map((button, index) => (
          <React.Fragment key={button.label}>
            <SelectionActionButton
              onClick={button.onClick}
              className={button.className}
            >
              {button.label}
            </SelectionActionButton>
            {index < actionButtons.length - 1 && <Divider />}
          </React.Fragment>
        ))}
        <Icons.Close height={20} width={20} />
      </div>
    </div>
  );
};

export default SelectionActionBar;
