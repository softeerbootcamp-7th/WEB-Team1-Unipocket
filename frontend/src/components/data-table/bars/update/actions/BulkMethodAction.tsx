import { ActionButton } from '@/components/data-table/bars/update/ActionButton';
import { ActionPopover } from '@/components/data-table/bars/update/ActionPopover';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';

import { useGetCardsQuery } from '@/api/users/query';

interface BulkMethodActionProps {
  onUpdate: (userCardId: number) => void;
}

export const BulkMethodAction = ({ onUpdate }: BulkMethodActionProps) => {
  const { data: cards = [] } = useGetCardsQuery();

  const handleMethodSelect = (cardNumber: string, close: () => void) => {
    const card = cards.find((c) => c.cardNumber === cardNumber);
    if (card && 'userCardId' in card) {
      onUpdate(card.userCardId as number);
    }
    close();
  };

  return (
    <ActionPopover
      renderContent={(close) => (
        <MethodSelectorContent
          align="center"
          sideOffset={16}
          onMethodSelect={(cardNumber) => handleMethodSelect(cardNumber, close)}
        />
      )}
    >
      <ActionButton>결제수단</ActionButton>
    </ActionPopover>
  );
};
