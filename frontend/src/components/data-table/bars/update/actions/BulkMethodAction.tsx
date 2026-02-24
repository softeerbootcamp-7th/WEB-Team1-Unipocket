import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { ActionPopover } from '@/components/data-table/bars/update/ActionPopover';
import { useDataTable } from '@/components/data-table/context';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';
import {
  getCardNumberFromExpense,
  resolveUserCardId,
} from '@/components/data-table/util';

import type { Expense } from '@/api/expenses/type';
import type { TempExpense } from '@/api/temporary-expenses/type';
import { useGetCardsQuery } from '@/api/users/query';
import { CASH } from '@/constants/column'; // CASH 상수 필요

interface BulkMethodActionProps {
  onUpdate: (userCardId: number | null, cardNumber: string) => void;
}

export const BulkMethodAction = ({ onUpdate }: BulkMethodActionProps) => {
  const { table } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;
  const { data: cards = [] } = useGetCardsQuery();

  const getInitialCardNumber = (): string | null => {
    if (selectedRows.length === 0) return null;

    const uniqueMethods = new Set(
      selectedRows.map((row) => {
        const original = row.original as Expense | TempExpense;

        if ('cardLastFourDigits' in original) {
          return original.cardLastFourDigits === ''
            ? CASH
            : original.cardLastFourDigits;
        }

        // Expense인 경우 기존 로직 사용
        return getCardNumberFromExpense(original, cards);
      }),
    );

    return uniqueMethods.size === 1 ? Array.from(uniqueMethods)[0] : null;
  };

  const handleMethodSelect = (cardNumber: string, close: () => void) => {
    const userCardId = resolveUserCardId(cardNumber, cards);

    if (userCardId !== undefined) {
      onUpdate(userCardId, cardNumber);
    }
    close();
  };

  return (
    <ActionPopover
      renderContent={(close) => (
        <MethodSelectorContent
          align="center"
          initialCardNumber={getInitialCardNumber()}
          sideOffset={16}
          onMethodSelect={(cardNumber) => handleMethodSelect(cardNumber, close)}
        />
      )}
    >
      <ActionButton>결제수단</ActionButton>
    </ActionPopover>
  );
};
