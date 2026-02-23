import { useState } from 'react';

import CardCreateModal from '@/components/setting-page/modal/CardCreateModal';
import CardDeleteModal from '@/components/setting-page/modal/CardDeleteModal';
import CardNicknameModal from '@/components/setting-page/modal/CardNicknameModal';
import { SettingSection } from '@/components/setting-page/SettingLayout';

import {
  useCardsSuspenseQuery,
  useCreateCardMutation,
  useDeleteCardMutation,
  useUpdateCardNicknameMutation,
} from '@/api/cards/query';
import type { Card } from '@/api/cards/type';
import { Cards, Icons } from '@/assets';

import Icon from '../common/Icon';

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

const LinkedCardList = () => {
  const { data: cards } = useCardsSuspenseQuery();
  const createCardMutation = useCreateCardMutation();
  const updateCardNicknameMutation = useUpdateCardNicknameMutation();
  const deleteCardMutation = useDeleteCardMutation();

  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<Card | null>(null);
  const [deletingCard, setDeletingCard] = useState<Card | null>(null);

  const handleCreateCard = (data: {
    cardCompany: string;
    cardNumber: string;
    nickName: string;
  }) => {
    createCardMutation.mutate(data, {
      onSuccess: () => setCreateModalOpen(false),
    });
  };

  const handleEditNickname = (cardId: number, nickName: string) => {
    updateCardNicknameMutation.mutate(
      { cardId, data: { nickname: nickName } },
      { onSuccess: () => setEditingCard(null) },
    );
  };

  const handleDeleteCard = (cardId: number) => {
    deleteCardMutation.mutate(cardId, {
      onSuccess: () => setDeletingCard(null),
    });
  };

  return (
    <SettingSection>
      <p className="heading2-bold text-label-normal w-50 shrink-0">
        국내카드 연동 목록
      </p>
      <div className="flex flex-col">
        {cards.length === 0 ? (
          <p className="body2-normal-regular text-label-assistive">
            아직 등록된 카드가 없어요.
          </p>
        ) : (
          cards.map((card) => (
            <LinkedCardItem
              key={card.userCardId}
              card={card}
              onEdit={setEditingCard}
              onDelete={setDeletingCard}
            />
          ))
        )}

        <button
          className="flex cursor-pointer items-center gap-4 py-3"
          onClick={() => setCreateModalOpen(true)}
        >
          <div className="rounded-modal-4 border-label-assistive flex h-12.25 w-19.5 items-center justify-center border">
            <Icons.Add className="text-label-assistive size-6" />
          </div>
          <span className="headline1-medium text-label-alternative">
            새 카드 추가
          </span>
        </button>
      </div>

      {isCreateModalOpen && (
        <CardCreateModal
          isSubmitting={createCardMutation.isPending}
          onClose={() => setCreateModalOpen(false)}
          onSubmit={handleCreateCard}
        />
      )}

      {editingCard && (
        <CardNicknameModal
          card={editingCard}
          isSubmitting={updateCardNicknameMutation.isPending}
          onClose={() => setEditingCard(null)}
          onSubmit={handleEditNickname}
        />
      )}

      {deletingCard && (
        <CardDeleteModal
          card={deletingCard}
          isSubmitting={deleteCardMutation.isPending}
          onClose={() => setDeletingCard(null)}
          onConfirm={handleDeleteCard}
        />
      )}
    </SettingSection>
  );
};

const CardListSkeleton = () => (
  <div className="h-24 w-full animate-pulse rounded-md bg-black/10" />
);

export { CardListSkeleton, LinkedCardList };
