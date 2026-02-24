import { useState } from 'react';

import { LinkedCardItem } from '@/components/setting-page/LinkedCardItem';
import CardCreateModal from '@/components/setting-page/modal/CardCreateModal';
import {
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

import {
  useCardsSuspenseQuery,
  useCreateCardMutation,
} from '@/api/cards/query';
import { Icons } from '@/assets';

interface LinkedCardListProps {
  openEditCardNickname: (cardId: number, currentNickname: string) => void;
  openDeleteCard: (cardId: number, cardNickname: string) => void;
}

const LinkedCardListSkeleton = () => (
  <SettingSection>
    <SettingTitle>국내카드 연동 목록</SettingTitle>
    <div className="bg-cool-neutral-97 rounded-modal-8 h-25 w-100 animate-pulse rounded-md" />
  </SettingSection>
);

const LinkedCardList = ({
  openEditCardNickname,
  openDeleteCard,
}: LinkedCardListProps) => {
  const { data: cards } = useCardsSuspenseQuery();
  const createCardMutation = useCreateCardMutation();

  const [isCreateModalOpen, setCreateModalOpen] = useState(false);

  const handleCreateCard = (data: {
    cardCompany: string;
    cardNumber: string;
    nickName: string;
  }) => {
    createCardMutation.mutate(data, {
      onSuccess: () => setCreateModalOpen(false),
    });
  };

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
              onEdit={(c) => openEditCardNickname(c.userCardId, c.nickName)}
              onDelete={(c) => openDeleteCard(c.userCardId, c.nickName)}
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

      <CardCreateModal
        isOpen={isCreateModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onSubmit={handleCreateCard}
      />
    </SettingSection>
  );
};

export { LinkedCardList, LinkedCardListSkeleton };
