import React from 'react';
import { clsx } from 'clsx';

import { BulkCategoryAction } from '@/components/data-table/bars/update/actions/BulkCategoryAction';
import { BulkMethodAction } from '@/components/data-table/bars/update/actions/BulkMethodAction';
import { useDataTable } from '@/components/data-table/context';

import { useBulkUpdateTempExpensesMutation } from '@/api/temporary-expenses/query';
import type { TempExpense } from '@/api/temporary-expenses/type';
import { Icons } from '@/assets';
import { CASH } from '@/constants/column';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const Divider = () => <div className="bg-line-solid-normal/30 h-6 w-px" />;

const ActionGroup = ({ children }: { children: React.ReactNode }) => {
  const childrenArray = React.Children.toArray(children).filter(Boolean);

  return (
    <>
      {childrenArray.map((child, index) => (
        <React.Fragment key={index}>
          {child}
          {index < childrenArray.length - 1 && <Divider />}
        </React.Fragment>
      ))}
    </>
  );
};

const TempUpdateActionBar = () => {
  const { table, tableState } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  const { accountBookId } = useRequiredAccountBook();

  const { mutate: bulkUpdateTemp } = useBulkUpdateTempExpensesMutation();

  const handleBulkUpdate = <K extends keyof TempExpense>(
    field: K,
    value: TempExpense[K],
  ) => {
    if (selectedRows.length === 0) return;

    // 1. 선택된 데이터에서 API URL에 필요한 metaId와 fileId 추출
    const firstRowOriginal = selectedRows[0].original as TempExpense;
    const metaId = firstRowOriginal.tempExpenseMetaId;
    const fileId = firstRowOriginal.fileId;

    const payloadItems = selectedRows.map((row) => {
      const original = row.original as TempExpense;
      return {
        tempExpenseId: original.tempExpenseId,
        [field]: value,
      };
    });

    bulkUpdateTemp(
      {
        accountBookId,
        metaId,
        fileId,
        data: { items: payloadItems },
      },
      {
        onSuccess: () => table.toggleAllRowsSelected(false),
      },
    );
  };

  if (!tableState.selectionMode) return null;

  return (
    <div
      className={clsx(
        'text-inverse-label body2-normal-bold truncate',
        'z-priority fixed bottom-30 left-1/2',
        'bg-inverse-background w-112.5 -translate-x-1/2 rounded-xl p-3 px-3.5 shadow-2xl',
      )}
    >
      <div className="flex items-center gap-4">
        <span className="mr-auto">{selectedRows.length}개 선택됨</span>

        <ActionGroup>
          <BulkCategoryAction
            onUpdate={(val) => handleBulkUpdate('category', val)}
          />
          <BulkMethodAction
            onUpdate={(_, cardNumber) => {
              const cardLastFourDigits = cardNumber === CASH ? '' : cardNumber;
              handleBulkUpdate('cardLastFourDigits', cardLastFourDigits);
            }}
          />
        </ActionGroup>

        <Icons.Close
          height={20}
          width={20}
          className="cursor-pointer"
          onClick={() => table.toggleAllRowsSelected(false)}
        />
      </div>
    </div>
  );
};

export default TempUpdateActionBar;
