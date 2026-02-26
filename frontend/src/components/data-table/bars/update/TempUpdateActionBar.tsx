import React, { useState } from 'react';
import { clsx } from 'clsx';

import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { BulkCategoryAction } from '@/components/data-table/bars/update/actions/BulkCategoryAction';
import { BulkMethodAction } from '@/components/data-table/bars/update/actions/BulkMethodAction';
import { useDataTable } from '@/components/data-table/context';
import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';

import {
  useBulkUpdateTempExpensesMutation,
  useDeleteTempExpenseMutation,
} from '@/api/temporary-expenses/query';
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
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const { table, tableState } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;

  const { accountBookId } = useRequiredAccountBook();

  const firstRowOriginal = selectedRows[0]?.original as TempExpense | undefined;
  const metaId = firstRowOriginal?.tempExpenseMetaId ?? 0;
  const fileId = firstRowOriginal?.fileId ?? 0;

  const { mutate: bulkUpdateTemp } = useBulkUpdateTempExpensesMutation();

  const { mutateAsync: deleteTempExpense } = useDeleteTempExpenseMutation();

  const handleBulkUpdate = <K extends keyof TempExpense>(
    field: K,
    value: TempExpense[K],
  ) => {
    if (selectedRows.length === 0) return;

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

  const confirmDelete = async () => {
    try {
      await Promise.allSettled(
        selectedRows.map((row) => {
          const original = row.original as TempExpense;
          return deleteTempExpense({
            accountBookId,
            metaId,
            fileId,
            tempExpenseId: original.tempExpenseId,
          });
        }),
      );
    } finally {
      table.toggleAllRowsSelected(false);
      setIsDeleteModalOpen(false);
    }
  };

  if (!tableState.selectionMode) return null;

  return (
    <>
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
                const cardLastFourDigits =
                  cardNumber === CASH ? '' : cardNumber;
                handleBulkUpdate('cardLastFourDigits', cardLastFourDigits);
              }}
            />
            <ActionButton
              onClick={() => setIsDeleteModalOpen(true)}
              className="text-status-negative"
            >
              삭제
            </ActionButton>
          </ActionGroup>

          <Icons.Close
            height={20}
            width={20}
            className="cursor-pointer"
            onClick={() => table.toggleAllRowsSelected(false)}
          />
        </div>
      </div>
      <TextConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onAction={confirmDelete}
        title="내역 삭제"
        description={`선택한 ${selectedRows.length}개의 내역을 삭제하시겠습니까?`}
        confirmButton={{ label: '삭제', variant: 'danger' }}
        backdropClassName="z-modal"
      />
    </>
  );
};

export default TempUpdateActionBar;
