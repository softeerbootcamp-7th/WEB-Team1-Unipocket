import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { ActionPopover } from '@/components/data-table/bars/update/ActionPopover';
import { useDataTable } from '@/components/data-table/context';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';
import {
  getCardNumberFromExpense,
  resolveUserCardId,
} from '@/components/data-table/util';

import type { Expense } from '@/api/expenses/type';
import { useGetCardsQuery } from '@/api/users/query';

interface BulkMethodActionProps {
  onUpdate: (userCardId: number | null) => void;
}

export const BulkMethodAction = ({ onUpdate }: BulkMethodActionProps) => {
  const { table } = useDataTable();
  const selectedRows = table.getFilteredSelectedRowModel().rows;
  const { data: cards = [] } = useGetCardsQuery();

  const getInitialCardNumber = (): string | null => {
    if (selectedRows.length === 0) return null;

    const uniqueMethods = new Set(
      selectedRows.map((row) =>
        getCardNumberFromExpense(row.original as Expense, cards),
      ),
    );

    return uniqueMethods.size === 1 ? Array.from(uniqueMethods)[0] : null;
  };

  const handleMethodSelect = (cardNumber: string, close: () => void) => {
    const userCardId = resolveUserCardId(cardNumber, cards);

    if (userCardId !== undefined) {
      onUpdate(userCardId);
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
