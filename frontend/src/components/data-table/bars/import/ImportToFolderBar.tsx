import { useParams } from '@tanstack/react-router';
import { clsx } from 'clsx';

import { useDataTable } from '@/components/data-table/context';

import { useBulkUpdateExpensesMutation } from '@/api/expenses/query';
import type { BulkUpdateExpenseItem, Expense } from '@/api/expenses/type';
import { Icons } from '@/assets';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const Divider = () => <div className="bg-line-solid-normal/30 h-6 w-px" />;

interface ImportToFolderBarProps {
  onSuccess?: () => void;
}

const ImportToFolderBar = ({ onSuccess }: ImportToFolderBarProps) => {
  const { table, tableState } = useDataTable();
  const { accountBookId } = useRequiredAccountBook();
  const selectedRows = table.getFilteredSelectedRowModel().rows;
  const { mutate: bulkUpdate } = useBulkUpdateExpensesMutation();

  const { travelId: travelIdParam } = useParams({
    from: '/_app/travel/$travelId',
  });
  const travelId = Number(travelIdParam);

  if (!tableState.selectionMode) return null;

  const handleImportToFolder = () => {
    const items: BulkUpdateExpenseItem[] = selectedRows.map((row) => {
      const original = row.original as Expense;

      return {
        expenseId: original.expenseId,
        merchantName: original.merchantName,
        category: original.category,
        occurredAt: original.occurredAt,
        localCurrencyAmount: original.localCurrencyAmount,
        localCurrencyCode: original.localCurrencyCode,
        baseCurrencyAmount: original.baseCurrencyAmount,
        memo: original.memo || '',
        travelId: travelId,
        userCardId: original.paymentMethod.isCash
          ? null
          : original.paymentMethod.card?.userCardId,
      };
    });

    bulkUpdate(
      { accountBookId, data: { items } },
      {
        onSuccess: () => {
          // 업데이트 성공 시 선택 초기화
          table.toggleAllRowsSelected(false);
          onSuccess?.();
        },
      },
    );
  };

  return (
    <div
      className={clsx(
        'body2-normal-bold truncate',
        'bg-background-normal rounded-modal-12 shadow-semantic-strong w-125 px-4 py-2.75',
        'z-priority fixed bottom-30 left-1/2 -translate-x-1/2',
      )}
    >
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
