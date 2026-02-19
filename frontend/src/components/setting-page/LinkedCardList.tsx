import { useState } from 'react';

import CardCreateModal from '@/components/setting-page/modal/CardCreateModal';
import CardDeleteModal from '@/components/setting-page/modal/CardDeleteModal';
import CardNicknameModal from '@/components/setting-page/modal/CardNicknameModal';
import {
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

import {
  useCardsSuspenseQuery,
  useCreateCardMutation,
  useDeleteCardMutation,
  useUpdateCardNicknameMutation,
} from '@/api/cards/query';
import type { Card } from '@/api/cards/type';
import { Cards, Icons } from '@/assets';

interface LinkedCardItemProps {
  card: Card;
  onEdit: (card: Card) => void;
  onDelete: (card: Card) => void;
}
const LinkedCardItem = ({ card, onEdit, onDelete }: LinkedCardItemProps) => {
  return (
    <div className="flex items-center justify-between border-b border-gray-100 py-3 last:border-0">
      <div className="flex items-center gap-4">
        <div className="h-10 w-16 overflow-hidden rounded-md border border-gray-200 bg-white">
          <Cards.Default className="h-full w-full object-cover" />
        </div>

        <div className="flex items-center gap-2 text-sm">
          <span className="font-semibold text-gray-900">
            {card.cardCompany}
          </span>
          <span className="text-gray-400">({card.cardNumber})</span>
          <span className="mx-1 text-gray-300">|</span>
          <span className="text-gray-500">{card.nickName}</span>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <button
          className="rounded-full p-2 transition-colors hover:bg-gray-100"
          onClick={() => onEdit(card)}
        >
          <Icons.Update className="h-5 w-5 text-gray-400" />
        </button>
        <button
          className="rounded-full p-2 text-red-400 transition-colors hover:bg-gray-100"
          onClick={() => onDelete(card)}
        >
          <Icons.Trash className="h-5 w-5" />
        </button>
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
      { cardId, data: { nickName } },
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
      <SettingTitle>국내카드 연동 목록</SettingTitle>
      <div className="flex w-full flex-1 flex-col">
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
          className="flex items-center gap-4 py-3"
          onClick={() => setCreateModalOpen(true)}
        >
          <div className="flex h-10 w-16 items-center justify-center rounded-md border border-dashed border-gray-300 bg-gray-50">
            <span className="text-xl text-gray-400">+</span>
          </div>
          <span className="text-sm text-gray-500">새 카드 추가</span>
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
