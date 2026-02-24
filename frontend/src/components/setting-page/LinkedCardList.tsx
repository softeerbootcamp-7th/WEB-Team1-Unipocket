import { useState } from 'react';

import { LinkedCardItem } from '@/components/setting-page/LinkedCardItem';
import CardCreateModal from '@/components/setting-page/modal/CardCreateModal';
import CardDeleteModal from '@/components/setting-page/modal/CardDeleteModal';
import CardNicknameModal from '@/components/setting-page/modal/CardNicknameModal';
import {
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

import {
  useCardsQuery,
  useCreateCardMutation,
  useDeleteCardMutation,
  useUpdateCardNicknameMutation,
} from '@/api/cards/query';
import type { Card } from '@/api/cards/type';
import { Icons } from '@/assets';

const LinkedCardList = () => {
  const { data: cards, isLoading } = useCardsQuery();
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

  if (isLoading || !cards) {
    return null;
  }

  return (
    <SettingSection>
      <SettingTitle>국내카드 연동 목록</SettingTitle>
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

export { LinkedCardList };
