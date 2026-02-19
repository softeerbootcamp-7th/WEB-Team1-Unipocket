import {
  type ComponentPropsWithoutRef,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import clsx from 'clsx';
import { toast } from 'sonner';

import Button from '@/components/common/Button';
import Dropdown from '@/components/common/dropdown/Dropdown';
import TextInput from '@/components/common/TextInput';
import Modal from '@/components/modal/Modal';
import { useModalContext } from '@/components/modal/useModalContext';

import {
  useAccountBookDetailQuery,
  useAccountBooksQuery,
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useSetMainAccountBookMutation,
  useUpdateAccountBookMutation,
} from '@/api/account-books/query';
import type {
  AccountBookDetail,
  AccountBookSummary,
} from '@/api/account-books/type';
import {
  useCardsQuery,
  useCreateCardMutation,
  useDeleteCardMutation,
  useUpdateCardNicknameMutation,
} from '@/api/cards/query';
import type { Card } from '@/api/cards/type';
import { Cards, Icons } from '@/assets';
import type { CountryCode } from '@/data/countryCode';
import countryData from '@/data/countryData.json';
import { getCountryInfo } from '@/lib/country';

const SettingPage = () => {
  const {
    data: accountBooks,
    isLoading: isAccountBooksLoading,
    isError: isAccountBooksError,
  } = useAccountBooksQuery();
  const {
    data: cards,
    isLoading: isCardsLoading,
    isError: isCardsError,
  } = useCardsQuery();

  const accountBooksErrorShown = useRef(false);
  const cardsErrorShown = useRef(false);

  useEffect(() => {
    if (isAccountBooksError && !accountBooksErrorShown.current) {
      toast.error('가계부 목록을 불러오지 못했어요.');
      accountBooksErrorShown.current = true;
    }
  }, [isAccountBooksError]);

  useEffect(() => {
    if (isCardsError && !cardsErrorShown.current) {
      toast.error('카드 목록을 불러오지 못했어요.');
      cardsErrorShown.current = true;
    }
  }, [isCardsError]);

  return (
    <div className="bg flex flex-1 flex-col px-30 py-8">
      <h1 className="title2-semibold text-label-normal mb-6.5">설정</h1>
      <div className="flex flex-col gap-3.5">
        <MainAccountBookSelection
          accountBooks={accountBooks ?? []}
          isLoading={isAccountBooksLoading}
        />
        <LinkedCardList cards={cards ?? []} isLoading={isCardsLoading} />
        <AccountBookConfigurator
          accountBooks={accountBooks ?? []}
          isLoading={isAccountBooksLoading}
        />
        <AccountManagement />
      </div>
    </div>
  );
};

export default SettingPage;

const MainAccountBookSelection = ({
  accountBooks,
  isLoading,
}: {
  accountBooks: AccountBookSummary[];
  isLoading: boolean;
}) => {
  const setMainMutation = useSetMainAccountBookMutation();

  const options = accountBooks.map((book) => ({
    id: book.id,
    name: book.title,
  }));

  const selectedId = accountBooks.find((book) => book.isMain)?.id ?? null;

  const handleSelect = (id: number) => {
    if (id === selectedId) return;
    setMainMutation.mutate(id);
  };

  return (
    <SettingSection>
      <SettingTitle>메인 가계부 설정</SettingTitle>
      <div className="flex w-full flex-1 flex-col gap-2">
        {isLoading ? (
          <p className="body2-normal-regular text-label-assistive">
            메인 가계부를 불러오는 중이에요.
          </p>
        ) : options.length === 0 ? (
          <p className="body2-normal-regular text-label-assistive">
            아직 가계부가 없어요.
          </p>
        ) : (
          <Dropdown
            selected={selectedId}
            onSelect={handleSelect}
            options={options}
          />
        )}
      </div>
    </SettingSection>
  );
};

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

const LinkedCardList = ({
  cards,
  isLoading,
}: {
  cards: Card[];
  isLoading: boolean;
}) => {
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
        {isLoading ? (
          <p className="body2-normal-regular text-label-assistive">
            카드 목록을 불러오는 중이에요.
          </p>
        ) : cards.length === 0 ? (
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

const AccountBookConfigurator = ({
  accountBooks,
  isLoading,
}: {
  accountBooks: AccountBookSummary[];
  isLoading: boolean;
}) => {
  const createAccountBookMutation = useCreateAccountBookMutation();
  const updateAccountBookMutation = useUpdateAccountBookMutation();
  const deleteAccountBookMutation = useDeleteAccountBookMutation();

  const [selectedAccountBookId, setSelectedAccountBookId] = useState<
    number | null
  >(null);
  const [isNameModalOpen, setNameModalOpen] = useState(false);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);

  const activeAccountBookId = accountBooks.some(
    (book) => book.id === selectedAccountBookId,
  )
    ? selectedAccountBookId
    : (accountBooks[0]?.id ?? null);

  const {
    data: accountBookDetail,
    isLoading: isDetailLoading,
    isError: isDetailError,
  } = useAccountBookDetailQuery(activeAccountBookId);
  const detailErrorShown = useRef(false);

  useEffect(() => {
    if (isDetailError && !detailErrorShown.current) {
      toast.error('가계부 상세 정보를 불러오지 못했어요.');
      detailErrorShown.current = true;
    }
  }, [isDetailError]);

  const countryOptions = useMemo(
    () =>
      Object.entries(countryData).map(([code, data], index) => ({
        id: index + 1,
        name: data.countryName,
        code: code as CountryCode,
      })),
    [],
  );

  const handleCreateAccountBook = (data: {
    localCountryCode: CountryCode;
    startDate: string;
    endDate: string;
  }) => {
    createAccountBookMutation.mutate(data, {
      onSuccess: () => setCreateModalOpen(false),
    });
  };

  const mainAccountBook = accountBooks.find((book) => book.isMain);
  const activeTitle =
    accountBooks.find((book) => book.id === activeAccountBookId)?.title ?? '';

  return (
    <SettingSection>
      <SettingTitle>가계부 설정</SettingTitle>
      <div className="flex w-full flex-1 flex-col gap-4">
        {isLoading ? (
          <p className="body2-normal-regular text-label-assistive">
            가계부 목록을 불러오는 중이에요.
          </p>
        ) : accountBooks.length === 0 ? (
          <div className="flex flex-col gap-3">
            <p className="body2-normal-regular text-label-assistive">
              아직 가계부가 없어요.
            </p>
            <Button
              size="sm"
              variant="solid"
              onClick={() => setCreateModalOpen(true)}
            >
              새 가계부 만들기
            </Button>
          </div>
        ) : (
          <>
            <div className="flex flex-wrap items-center gap-4">
              {accountBooks.map((book) => (
                <button
                  key={book.id}
                  onClick={() => setSelectedAccountBookId(book.id)}
                  className={clsx(
                    'body2-normal-medium pb-3.5',
                    book.id === activeAccountBookId
                      ? 'border-label-normal border-b-2'
                      : 'text-label-assistive',
                  )}
                >
                  {book.title}
                  {book.isMain && (
                    <span className="caption1-regular text-primary-normal ml-1">
                      (메인)
                    </span>
                  )}
                </button>
              ))}
              <Button
                size="sm"
                variant="outlined"
                onClick={() => setCreateModalOpen(true)}
              >
                새 가계부 추가
              </Button>
            </div>

            {isDetailLoading || !accountBookDetail ? (
              <p className="body2-normal-regular text-label-assistive">
                가계부 상세 정보를 불러오는 중이에요.
              </p>
            ) : (
              <AccountBookSettingsForm
                key={`${accountBookDetail.id}-${accountBookDetail.title}-${accountBookDetail.localCountryCode}-${accountBookDetail.baseCountryCode}-${accountBookDetail.startDate}-${accountBookDetail.endDate}`}
                detail={accountBookDetail}
                countryOptions={countryOptions}
                isSaving={updateAccountBookMutation.isPending}
                isDeleting={deleteAccountBookMutation.isPending}
                onOpenNameModal={() => setNameModalOpen(true)}
                onOpenDeleteModal={() => setDeleteModalOpen(true)}
                onSave={(updated) =>
                  updateAccountBookMutation.mutate({
                    accountBookId: updated.id,
                    data: {
                      title: updated.title,
                      localCountryCode: updated.localCountryCode,
                      baseCountryCode: updated.baseCountryCode,
                      startDate: updated.startDate,
                      endDate: updated.endDate,
                    },
                  })
                }
              />
            )}
          </>
        )}
      </div>

      {isNameModalOpen && (
        <AccountBookNameModal
          accountBooks={accountBooks}
          accountBookId={activeAccountBookId}
          currentTitle={accountBookDetail?.title ?? activeTitle}
          isSubmitting={updateAccountBookMutation.isPending}
          onClose={() => setNameModalOpen(false)}
          onSubmit={(title) => {
            if (!activeAccountBookId) return;
            updateAccountBookMutation.mutate(
              { accountBookId: activeAccountBookId, data: { title } },
              { onSuccess: () => setNameModalOpen(false) },
            );
          }}
        />
      )}

      {isDeleteModalOpen && (
        <AccountBookDeleteModal
          accountBookTitle={activeTitle}
          isMain={mainAccountBook?.id === activeAccountBookId}
          isSubmitting={deleteAccountBookMutation.isPending}
          onClose={() => setDeleteModalOpen(false)}
          onConfirm={() => {
            if (!activeAccountBookId) return;
            deleteAccountBookMutation.mutate(activeAccountBookId, {
              onSuccess: () => setDeleteModalOpen(false),
            });
          }}
        />
      )}

      {isCreateModalOpen && (
        <AccountBookCreateModal
          isSubmitting={createAccountBookMutation.isPending}
          onClose={() => setCreateModalOpen(false)}
          onSubmit={handleCreateAccountBook}
        />
      )}
    </SettingSection>
  );
};

const AccountBookSettingsForm = ({
  detail,
  countryOptions,
  isSaving,
  isDeleting,
  onOpenNameModal,
  onOpenDeleteModal,
  onSave,
}: {
  detail: AccountBookDetail;
  countryOptions: Array<{ id: number; name: string; code: CountryCode }>;
  isSaving: boolean;
  isDeleting: boolean;
  onOpenNameModal: () => void;
  onOpenDeleteModal: () => void;
  onSave: (detail: AccountBookDetail) => void;
}) => {
  const [formState, setFormState] = useState<AccountBookDetail>(detail);

  const selectedCountryId =
    countryOptions.find((option) => option.code === formState.localCountryCode)
      ?.id ??
    countryOptions[0]?.id ??
    null;

  const handleCountrySelect = (id: number) => {
    const selected = countryOptions.find((option) => option.id === id);
    if (!selected) return;
    setFormState((prev) => ({
      ...prev,
      localCountryCode: selected.code,
      baseCountryCode: selected.code,
    }));
  };

  return (
    <div className="border-line-normal-neutral flex flex-col gap-4 rounded-xl border p-4">
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-1">
          <p className="label1-normal-bold text-label-neutral">가계부 이름</p>
          <p className="body2-normal-regular text-label-normal">
            {formState.title}
          </p>
        </div>
        <Button size="xs" variant="outlined" onClick={onOpenNameModal}>
          이름 변경
        </Button>
      </div>

      <div className="flex flex-col gap-2">
        <p className="label1-normal-bold text-label-neutral">국가 변경</p>
        <Dropdown
          selected={selectedCountryId}
          onSelect={handleCountrySelect}
          options={countryOptions}
        />
      </div>

      <div className="flex flex-col gap-2">
        <p className="label1-normal-bold text-label-neutral">통화 정보</p>
        <div className="text-label-normal flex flex-wrap gap-3 text-sm">
          <span>
            현지 통화:{' '}
            {getCountryInfo(formState.localCountryCode)?.currencyName ?? '-'}
          </span>
          <span>
            기준 통화:{' '}
            {getCountryInfo(formState.baseCountryCode)?.currencyName ?? '-'}
          </span>
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <p className="label1-normal-bold text-label-neutral">기간 설정</p>
        <div className="flex flex-wrap gap-3">
          <input
            type="date"
            className="bg-background-normal body2-normal-regular border-line-normal-neutral h-12 rounded-xl border px-3"
            value={formState.startDate}
            onChange={(e) =>
              setFormState((prev) => ({
                ...prev,
                startDate: e.target.value,
              }))
            }
          />
          <input
            type="date"
            className="bg-background-normal body2-normal-regular border-line-normal-neutral h-12 rounded-xl border px-3"
            value={formState.endDate}
            onChange={(e) =>
              setFormState((prev) => ({
                ...prev,
                endDate: e.target.value,
              }))
            }
          />
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <Button
          size="sm"
          variant="solid"
          onClick={() => onSave(formState)}
          disabled={isSaving}
        >
          변경사항 저장
        </Button>
        <Button
          size="sm"
          variant="danger"
          onClick={onOpenDeleteModal}
          disabled={isDeleting}
        >
          가계부 삭제
        </Button>
      </div>
    </div>
  );
};
const AccountManagement = () => {
  return (
    <SettingSection>
      <SettingTitle disabled>계정 삭제</SettingTitle>
    </SettingSection>
  );
};

const SettingSection = ({ children }: { children: React.ReactNode }) => {
  return <div className="flex py-2.5">{children}</div>;
};

interface SettingTitleProps extends ComponentPropsWithoutRef<'h1'> {
  disabled?: boolean;
}

const SettingTitle = ({ children, disabled = false }: SettingTitleProps) => {
  return (
    <h1
      className={clsx('heading2-bold w-50', {
        'text-label-assistive': disabled,
        'text-label-normal': !disabled,
      })}
    >
      {children}
    </h1>
  );
};

const CardCreateModal = ({
  onClose,
  onSubmit,
  isSubmitting,
}: {
  onClose: () => void;
  onSubmit: (data: {
    cardCompany: string;
    cardNumber: string;
    nickName: string;
  }) => void;
  isSubmitting: boolean;
}) => {
  const [cardCompany, setCardCompany] = useState('');
  const [cardNumber, setCardNumber] = useState('');
  const [nickName, setNickName] = useState('');

  const isValid =
    cardCompany.trim().length > 0 &&
    cardNumber.trim().length > 0 &&
    nickName.trim().length > 0;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() =>
        onSubmit({
          cardCompany: cardCompany.trim(),
          cardNumber: cardNumber.trim(),
          nickName: nickName.trim(),
        })
      }
      confirmButton={{ label: '등록', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">카드 등록</p>
          <TextInput
            title="카드사"
            placeholder="SHINHAN"
            value={cardCompany}
            onChange={setCardCompany}
            isError={cardCompany.trim().length === 0}
            errorMessage="카드사를 입력해주세요."
          />
          <TextInput
            title="카드 번호"
            placeholder="1433"
            value={cardNumber}
            onChange={setCardNumber}
            isError={cardNumber.trim().length === 0}
            errorMessage="카드 번호를 입력해주세요."
          />
          <TextInput
            title="별명"
            placeholder="별명을 입력하세요"
            value={nickName}
            onChange={setNickName}
            isError={nickName.trim().length === 0}
            errorMessage="별명을 입력해주세요."
          />
        </div>
      </ModalFormContent>
    </Modal>
  );
};

const CardNicknameModal = ({
  card,
  isSubmitting,
  onClose,
  onSubmit,
}: {
  card: Card;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (cardId: number, nickName: string) => void;
}) => {
  const [nickName, setNickName] = useState(card.nickName);

  const isValid = nickName.trim().length > 0;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => onSubmit(card.userCardId, nickName.trim())}
      confirmButton={{ label: '저장', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">카드 별명 수정</p>
          <TextInput
            title="별명"
            placeholder="별명을 입력하세요"
            value={nickName}
            onChange={setNickName}
            isError={!isValid}
            errorMessage="별명을 입력해주세요."
          />
        </div>
      </ModalFormContent>
    </Modal>
  );
};

const CardDeleteModal = ({
  card,
  isSubmitting,
  onClose,
  onConfirm,
}: {
  card: Card;
  isSubmitting: boolean;
  onClose: () => void;
  onConfirm: (cardId: number) => void;
}) => {
  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => onConfirm(card.userCardId)}
      confirmButton={{ label: '삭제', variant: 'danger' }}
    >
      <ModalFormContent isActionReady={!isSubmitting}>
        <div className="flex w-80 flex-col gap-2 px-2">
          <p className="heading2-bold text-label-normal">카드 삭제</p>
          <p className="body2-normal-regular text-label-assistive">
            {card?.nickName} 카드를 정말 삭제하시겠어요?
          </p>
        </div>
      </ModalFormContent>
    </Modal>
  );
};

const AccountBookNameModal = ({
  accountBooks,
  accountBookId,
  currentTitle,
  isSubmitting,
  onClose,
  onSubmit,
}: {
  accountBooks: AccountBookSummary[];
  accountBookId: number | null;
  currentTitle: string;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (title: string) => void;
}) => {
  const [title, setTitle] = useState(currentTitle);

  const isDuplicate = accountBooks.some(
    (book) => book.id !== accountBookId && book.title === title.trim(),
  );
  const isTooLong = title.trim().length > 10;
  const isValid = title.trim().length > 0 && !isDuplicate && !isTooLong;

  const errorMessage = isTooLong
    ? '최대 10자까지 입력할 수 있어요.'
    : isDuplicate
      ? '이미 동일한 이름의 가계부가 있어요.'
      : title.trim().length === 0
        ? '이름을 입력해주세요.'
        : '';

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => onSubmit(title.trim())}
      confirmButton={{ label: '저장', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">가계부 이름 변경</p>
          <TextInput
            title="가계부 이름"
            placeholder="최대 10자"
            value={title}
            onChange={setTitle}
            isError={!isValid}
            errorMessage={errorMessage}
          />
        </div>
      </ModalFormContent>
    </Modal>
  );
};

const AccountBookDeleteModal = ({
  accountBookTitle,
  isMain,
  isSubmitting,
  onClose,
  onConfirm,
}: {
  accountBookTitle: string;
  isMain: boolean;
  isSubmitting: boolean;
  onClose: () => void;
  onConfirm: () => void;
}) => {
  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={onConfirm}
      confirmButton={{ label: '삭제', variant: 'danger' }}
    >
      <ModalFormContent isActionReady={!isSubmitting}>
        <div className="flex w-80 flex-col gap-2 px-2">
          <p className="heading2-bold text-label-normal">가계부 삭제</p>
          <p className="body2-normal-regular text-label-assistive">
            {accountBookTitle} 가계부를 정말 삭제하시겠어요?
          </p>
          {isMain && (
            <p className="caption1-regular text-label-assistive">
              메인 가계부를 삭제하면 다음 가계부가 메인으로 설정돼요.
            </p>
          )}
        </div>
      </ModalFormContent>
    </Modal>
  );
};

const AccountBookCreateModal = ({
  isSubmitting,
  onClose,
  onSubmit,
}: {
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (data: {
    localCountryCode: CountryCode;
    startDate: string;
    endDate: string;
  }) => void;
}) => {
  const countryOptions = useMemo(
    () =>
      Object.entries(countryData).map(([code, data], index) => ({
        id: index + 1,
        name: data.countryName,
        code: code as CountryCode,
      })),
    [],
  );

  const [selectedCountryId, setSelectedCountryId] = useState<number | null>(
    countryOptions[0]?.id ?? null,
  );
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const selectedCountry = countryOptions.find(
    (option) => option.id === selectedCountryId,
  );

  const isValid =
    !!selectedCountry &&
    startDate.trim().length > 0 &&
    endDate.trim().length > 0;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() =>
        selectedCountry &&
        onSubmit({
          localCountryCode: selectedCountry.code,
          startDate,
          endDate,
        })
      }
      confirmButton={{ label: '생성', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">새 가계부 만들기</p>
          <div className="flex flex-col gap-2">
            <p className="label1-normal-bold text-label-neutral">국가 선택</p>
            <Dropdown
              selected={selectedCountryId}
              onSelect={setSelectedCountryId}
              options={countryOptions}
            />
          </div>
          <div className="flex flex-col gap-2">
            <p className="label1-normal-bold text-label-neutral">기간 설정</p>
            <div className="flex flex-wrap gap-3">
              <input
                type="date"
                className="bg-background-normal body2-normal-regular border-line-normal-neutral h-12 rounded-xl border px-3"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
              <input
                type="date"
                className="bg-background-normal body2-normal-regular border-line-normal-neutral h-12 rounded-xl border px-3"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
          </div>
        </div>
      </ModalFormContent>
    </Modal>
  );
};

const ModalFormContent = ({
  isActionReady,
  children,
}: {
  isActionReady: boolean;
  children: React.ReactNode;
}) => {
  const { setActionReady } = useModalContext();

  useEffect(() => {
    setActionReady(isActionReady);
  }, [isActionReady, setActionReady]);

  return <div className="pb-4">{children}</div>;
};
