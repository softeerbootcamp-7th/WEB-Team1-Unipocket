import Icon from '@/components/common/Icon';

import type { Card } from '@/api/cards/type';
import { Cards } from '@/assets';

interface LinkedCardItemProps {
  card: Card;
  onEdit: (card: Card) => void;
  onDelete: (card: Card) => void;
}

const LinkedCardItem = ({ card, onEdit, onDelete }: LinkedCardItemProps) => {
  return (
    <div className="flex w-131.5 items-center justify-between px-px py-3">
      <div className="flex items-center gap-5.5">
        <div className="rounded-modal-4 border-line-normal-normal bg-background-normal h-12.25 w-19.5 border">
          <Cards.Default className="text-cool-neutral-70 size-full" />
        </div>

        <div className="flex items-center gap-3.5">
          <div className="flex items-center gap-2">
            <span className="headline1-medium text-label-normal">
              {card.cardCompany}
            </span>
            <span className="headline1-medium text-label-alternative">
              ({card.cardNumber})
            </span>
          </div>
          <span className="bg-line-normal-normal h-5 w-px"></span>
          <span className="headline1-medium text-label-alternative">
            {card.nickName}
          </span>
        </div>
      </div>

      <div className="flex items-center gap-3.5 px-1.5 py-1">
        <Icon
          iconName="Update"
          className="h-5 w-5 text-gray-400"
          onClick={() => onEdit(card)}
        />
        <Icon
          iconName="Trash"
          className="h-5 w-5 text-red-400"
          onClick={() => onDelete(card)}
        />
      </div>
    </div>
  );
};

export { LinkedCardItem };
export type { LinkedCardItemProps };
