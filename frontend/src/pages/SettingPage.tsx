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
import AccountBookCountryModal from '@/components/setting-page/modal/AccountBookCountryModal';
import AccountBookCreateModal from '@/components/setting-page/modal/AccountBookCreateModal';
import AccountBookCurrencyModal from '@/components/setting-page/modal/AccountBookCurrencyModal';
import AccountBookDeleteModal from '@/components/setting-page/modal/AccountBookDeleteModal';
import AccountBookNameModal from '@/components/setting-page/modal/AccountBookNameModal';
import AccountBookPeriodModal from '@/components/setting-page/modal/AccountBookPeriodModal';
import CardCreateModal from '@/components/setting-page/modal/CardCreateModal';
import CardDeleteModal from '@/components/setting-page/modal/CardDeleteModal';
import CardNicknameModal from '@/components/setting-page/modal/CardNicknameModal';

import {
  useAccountBookDetailQuery,
  useAccountBooksQuery,
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useUpdateAccountBookMutation,
} from '@/api/account-books/query';
import type {
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
} from '@/api/account-books/type';
import {
  useCardsQuery,
  useCreateCardMutation,
  useDeleteCardMutation,
  useUpdateCardNicknameMutation,
} from '@/api/cards/query';
import type { Card } from '@/api/cards/type';
import { Cards, Icons } from '@/assets';
import type { CountryCode } from '@/data/country/countryCode';
import countryData from '@/data/country/countryData.json';
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
  accountBooks: GetAccountBooksResponse;
  isLoading: boolean;
}) => {
  // const setMainMutation = useSetMainAccountBookMutation();

  const options = accountBooks.map((book) => ({
    id: book.id,
    name: book.title,
  }));

  const selectedId = accountBooks.find((book) => book.isMain)?.id ?? 0;

  const handleSelect = (id: number) => {
    if (id === selectedId) return;
    // setMainMutation.mutate(id);
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
            selectedId={selectedId}
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
  accountBooks: GetAccountBooksResponse;
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
  const [isCurrencyModalOpen, setCurrencyModalOpen] = useState(false);
  const [isCountryModalOpen, setCountryModalOpen] = useState(false);
  const [isPeriodModalOpen, setPeriodModalOpen] = useState(false);

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
      <div className="flex w-full flex-1 flex-col">
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
            <div className="border-line-normal-neutral flex flex-wrap items-end gap-4 border-b">
              {accountBooks.map((book) => (
                <button
                  key={book.id}
                  onClick={() => setSelectedAccountBookId(book.id)}
                  className={clsx(
                    'body2-normal-medium flex items-center gap-1 pb-3',
                    book.id === activeAccountBookId
                      ? 'border-label-normal text-label-normal border-b-2'
                      : 'text-label-assistive',
                  )}
                >
                  {book.title}
                  {book.isMain && (
                    <span className="caption1-regular bg-primary-normal rounded-full px-1.5 py-0.5 text-white">
                      메인
                    </span>
                  )}
                </button>
              ))}
              <button
                onClick={() => setCreateModalOpen(true)}
                className="body2-normal-regular text-label-assistive pb-3"
              >
                + 새 가계부 추가
              </button>
            </div>

            {isDetailLoading || !accountBookDetail ? (
              <p className="body2-normal-regular text-label-assistive pt-3">
                가계부 상세 정보를 불러오는 중이에요.
              </p>
            ) : (
              <AccountBookSettingsForm
                key={accountBookDetail.id}
                detail={accountBookDetail}
                onOpenNameModal={() => setNameModalOpen(true)}
                onOpenDeleteModal={() => setDeleteModalOpen(true)}
                onOpenCurrencyModal={() => setCurrencyModalOpen(true)}
                onOpenCountryModal={() => setCountryModalOpen(true)}
                onOpenPeriodModal={() => setPeriodModalOpen(true)}
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

      {isCurrencyModalOpen && accountBookDetail && (
        <AccountBookCurrencyModal
          countryOptions={countryOptions}
          currentBaseCountryCode={accountBookDetail.baseCountryCode}
          isSubmitting={updateAccountBookMutation.isPending}
          onClose={() => setCurrencyModalOpen(false)}
          onSubmit={(baseCountryCode) => {
            if (!activeAccountBookId) return;
            updateAccountBookMutation.mutate(
              { accountBookId: activeAccountBookId, data: { baseCountryCode } },
              { onSuccess: () => setCurrencyModalOpen(false) },
            );
          }}
        />
      )}

      {isCountryModalOpen && accountBookDetail && (
        <AccountBookCountryModal
          countryOptions={countryOptions}
          currentLocalCountryCode={accountBookDetail.localCountryCode}
          isSubmitting={updateAccountBookMutation.isPending}
          onClose={() => setCountryModalOpen(false)}
          onSubmit={(localCountryCode) => {
            if (!activeAccountBookId) return;
            updateAccountBookMutation.mutate(
              {
                accountBookId: activeAccountBookId,
                data: { localCountryCode },
              },
              { onSuccess: () => setCountryModalOpen(false) },
            );
          }}
        />
      )}

      {isPeriodModalOpen && accountBookDetail && (
        <AccountBookPeriodModal
          currentStartDate={accountBookDetail.startDate}
          currentEndDate={accountBookDetail.endDate}
          isSubmitting={updateAccountBookMutation.isPending}
          onClose={() => setPeriodModalOpen(false)}
          onSubmit={(startDate, endDate) => {
            if (!activeAccountBookId) return;
            updateAccountBookMutation.mutate(
              {
                accountBookId: activeAccountBookId,
                data: { startDate, endDate },
              },
              { onSuccess: () => setPeriodModalOpen(false) },
            );
          }}
        />
      )}
    </SettingSection>
  );
};

const SettingRow = ({
  label,
  value,
  onEdit,
}: {
  label: string;
  value: string;
  onEdit: () => void;
}) => {
  return (
    <div className="border-line-normal-neutral flex items-center border-b py-3">
      <span className="label1-normal-bold text-label-neutral w-40 shrink-0">
        {label}
      </span>
      <span className="body2-normal-regular text-label-normal flex-1">
        {value}
      </span>
      <button
        onClick={onEdit}
        className="caption1-regular text-primary-normal shrink-0"
      >
        수정
      </button>
    </div>
  );
};

const AccountBookSettingsForm = ({
  detail,
  onOpenNameModal,
  onOpenDeleteModal,
  onOpenCurrencyModal,
  onOpenCountryModal,
  onOpenPeriodModal,
}: {
  detail: GetAccountBookDetailResponse;
  onOpenNameModal: () => void;
  onOpenDeleteModal: () => void;
  onOpenCurrencyModal: () => void;
  onOpenCountryModal: () => void;
  onOpenPeriodModal: () => void;
}) => {
  const baseCurrencyInfo = getCountryInfo(detail.baseCountryCode);
  const localCountryInfo = getCountryInfo(detail.localCountryCode);

  const formatDate = (dateStr: string) => dateStr.replace(/-/g, '.');

  const currencyDisplay = baseCurrencyInfo
    ? `${baseCurrencyInfo.currencyNameKor} ${baseCurrencyInfo.currencySign} ${baseCurrencyInfo.currencyName}`
    : '-';

  const countryDisplay = localCountryInfo?.countryName ?? '-';

  const periodDisplay = `${formatDate(detail.startDate)} - ${formatDate(detail.endDate)}`;

  return (
    <div className="flex flex-col">
      <SettingRow
        label="이름 수정"
        value={detail.title}
        onEdit={onOpenNameModal}
      />
      <SettingRow
        label="기준 통화 변경"
        value={currencyDisplay}
        onEdit={onOpenCurrencyModal}
      />
      <SettingRow
        label="국가/통화 변경"
        value={countryDisplay}
        onEdit={onOpenCountryModal}
      />
      <SettingRow
        label="카드 연동 기간 변경"
        value={periodDisplay}
        onEdit={onOpenPeriodModal}
      />
      <button
        onClick={onOpenDeleteModal}
        className="body2-normal-regular text-status-negative py-3 text-left"
      >
        {detail.title} 삭제
      </button>
      <p className="caption1-regular text-label-assistive mt-2">
        * 한 가계부 안에 가계부를 추가로 만들 수 있어요.
      </p>
      <p className="caption1-regular text-label-assistive">
        * 가계부 삭제 시 가계부에 저장된 모든 내역이 삭제돼요.
      </p>
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
