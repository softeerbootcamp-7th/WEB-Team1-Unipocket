import { useEffect, useState } from 'react';
import clsx from 'clsx';

import TextInput from '@/components/common/TextInput';
import Modal from '@/components/modal/Modal';
import { useModalContext } from '@/components/modal/useModalContext';
import { Checkbox } from '@/components/ui/checkbox';

import { type CardId, CARDS } from '@/data/card/cardCode';

interface CardCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: {
    cardCompany: string;
    cardNumber: string;
    nickName: string;
  }) => void;
}

const CARD_ENTRIES = Object.entries(CARDS) as [
  string,
  (typeof CARDS)[CardId],
][];

const CardListContent = ({
  selectedId,
  onSelect,
  nickname,
  onNicknameChange,
  cardNumber,
  onCardNumberChange,
}: {
  selectedId: string;
  onSelect: (id: string) => void;
  nickname: string;
  onNicknameChange: (v: string) => void;
  cardNumber: string;
  onCardNumberChange: (v: string) => void;
}) => {
  const { setActionReady } = useModalContext();

  const nicknameError =
    nickname.length > 10 ? '공백 포함 최대 10자까지 입력 가능해요.' : undefined;
  const cardNumberError =
    cardNumber.length > 0 && !/^\d{4}$/.test(cardNumber)
      ? '숫자 4자리를 입력해주세요.'
      : undefined;

  const isValid =
    selectedId !== '' &&
    nickname.trim().length > 0 &&
    !nicknameError &&
    /^\d{4}$/.test(cardNumber);

  useEffect(() => {
    setActionReady(isValid);
  }, [isValid, setActionReady]);

  return (
    <div className="flex min-w-118 flex-col gap-5 pb-2">
      <h2 className="text-label-normal headline1-bold text-center">
        카드 등록
      </h2>

      {/* 금융사 선택 */}
      <div className="flex flex-col gap-2">
        <p className="label1-normal-bold text-label-neutral">금융사 선택</p>
        <div className="border-line-normal-neutral rounded-modal-8 h-[calc(5*4rem)] overflow-y-auto border">
          {CARD_ENTRIES.map(([id, card]) => (
            <div
              key={id}
              className={clsx(
                'border-line-normal-normal flex cursor-pointer items-center gap-4 border-b px-4 py-3.5 last:border-b-0',
              )}
              onClick={() => onSelect(id)}
            >
              <Checkbox
                checked={selectedId === id}
                onCheckedChange={() => onSelect(id)}
                className="size-5"
              />
              <img
                src={card.logo}
                alt={card.code}
                className="h-8 w-12 object-contain"
              />
              <span className="headline2-medium text-label-normal">
                {card.code}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* 닉네임 */}
      <TextInput
        title="카드 별명"
        placeholder="별명을 입력하세요 (최대 10자)"
        value={nickname}
        onChange={onNicknameChange}
        isError={!!nicknameError}
        errorMessage={nicknameError}
      />

      {/* 카드 번호 뒤 4자리 */}
      <TextInput
        title="카드 번호 뒤 4자리"
        placeholder="0000"
        value={cardNumber}
        onChange={(v) => {
          if (/^\d{0,4}$/.test(v)) onCardNumberChange(v);
        }}
        isError={!!cardNumberError}
        errorMessage={cardNumberError}
      />
    </div>
  );
};

const CardCreateModal = ({
  isOpen,
  onClose,
  onSubmit,
}: CardCreateModalProps) => {
  const [selectedId, setSelectedId] = useState('');
  const [nickname, setNickname] = useState('');
  const [cardNumber, setCardNumber] = useState('');

  const [prevIsOpen, setPrevIsOpen] = useState(false);
  if (isOpen !== prevIsOpen) {
    setPrevIsOpen(isOpen);
    if (isOpen) {
      setSelectedId('');
      setNickname('');
      setCardNumber('');
    }
  }

  const handleAction = () => {
    onSubmit({
      cardCompany: selectedId,
      cardNumber,
      nickName: nickname.trim(),
    });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      onAction={handleAction}
      confirmButton={{ label: '등록', variant: 'solid' }}
    >
      <CardListContent
        selectedId={selectedId}
        onSelect={setSelectedId}
        nickname={nickname}
        onNicknameChange={setNickname}
        cardNumber={cardNumber}
        onCardNumberChange={setCardNumber}
      />
    </Modal>
  );
};

export default CardCreateModal;
