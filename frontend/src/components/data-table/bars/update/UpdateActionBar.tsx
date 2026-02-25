import React, { useState } from 'react';
import { clsx } from 'clsx';

import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { BulkCategoryAction } from '@/components/data-table/bars/update/actions/BulkCategoryAction';
import { BulkMethodAction } from '@/components/data-table/bars/update/actions/BulkMethodAction';
import { BulkTravelAction } from '@/components/data-table/bars/update/actions/BulkTravelAction';
import { useDataTable } from '@/components/data-table/context';
import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';

import {
  useBulkUpdateExpensesMutation,
  useDeleteExpenseMutation,
} from '@/api/expenses/query';
import type { BulkUpdateExpenseItem, Expense } from '@/api/expenses/type';
import { Icons } from '@/assets';
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

const UpdateActionBar = () => {
  const { table, tableState } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  const { accountBookId } = useRequiredAccountBook();
  const { mutate: bulkUpdate } = useBulkUpdateExpensesMutation();
  const { mutateAsync: deleteExpense } = useDeleteExpenseMutation();

  const handleBulkUpdate = <
    K extends keyof Omit<BulkUpdateExpenseItem, 'expenseId'>,
  >(
    field: K,
    value: BulkUpdateExpenseItem[K],
  ) => {
    const items: BulkUpdateExpenseItem[] = selectedRows.map((row) => {
      const original = row.original as Expense;
      const basePayload: BulkUpdateExpenseItem = {
        expenseId: original.expenseId,
        merchantName: original.merchantName,
        category: original.category,
        occurredAt: original.occurredAt,
        localCurrencyAmount: original.localCurrencyAmount,
        localCurrencyCode: original.localCurrencyCode,
        baseCurrencyAmount: original.baseCurrencyAmount,
        travelId: original.travel?.travelId || null,
        userCardId: original.paymentMethod.isCash
          ? null
          : original.paymentMethod.card?.userCardId,
      };

      return { ...basePayload, [field]: value };
    });

    bulkUpdate(
      { accountBookId, data: { items } },
      { onSuccess: () => table.toggleAllRowsSelected(false) },
    );
  };

  const confirmDelete = async () => {
    try {
      await Promise.allSettled(
        selectedRows.map((row) =>
          deleteExpense({
            accountBookId,
            expenseId: (row.original as Expense).expenseId,
          }),
        ),
      );
    } finally {
      table.toggleAllRowsSelected(false);
      setIsDeleteModalOpen(false); // 삭제 후 모달 닫기
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
              onUpdate={(userCardId) =>
                handleBulkUpdate('userCardId', userCardId)
              }
            />
            <BulkTravelAction
              onUpdate={(val) => handleBulkUpdate('travelId', val)}
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
      />
    </>
  );
};

export default UpdateActionBar;
