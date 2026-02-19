import { useEffect, useMemo, useRef, useState } from 'react';
import clsx from 'clsx';
import { toast } from 'sonner';

import Button from '@/components/common/Button';
import AccountBookCountryModal from '@/components/setting-page/modal/AccountBookCountryModal';
import AccountBookCreateModal from '@/components/setting-page/modal/AccountBookCreateModal';
import AccountBookCurrencyModal from '@/components/setting-page/modal/AccountBookCurrencyModal';
import AccountBookDeleteModal from '@/components/setting-page/modal/AccountBookDeleteModal';
import AccountBookNameModal from '@/components/setting-page/modal/AccountBookNameModal';
import AccountBookPeriodModal from '@/components/setting-page/modal/AccountBookPeriodModal';
import {
  SettingRow,
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

import {
  useAccountBookDetailQuery,
  useAccountBooksSuspenseQuery,
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useUpdateAccountBookMutation,
} from '@/api/account-books/query';
import type { AccountBookDetail } from '@/api/account-books/type';
import type { CountryCode } from '@/data/countryCode';
import countryData from '@/data/countryData.json';
import { getCountryInfo } from '@/lib/country';

const ConfiguratorSkeleton = () => (
  <div className="h-32 w-full animate-pulse rounded-md bg-black/10" />
);

const AccountBookConfigurator = () => {
  const { data: accountBooks } = useAccountBooksSuspenseQuery();
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
        {accountBooks.length === 0 ? (
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

interface AccountBookSettingsFormProps {
  detail: AccountBookDetail;
  onOpenNameModal: () => void;
  onOpenDeleteModal: () => void;
  onOpenCurrencyModal: () => void;
  onOpenCountryModal: () => void;
  onOpenPeriodModal: () => void;
}

const AccountBookSettingsForm = ({
  detail,
  onOpenNameModal,
  onOpenDeleteModal,
  onOpenCurrencyModal,
  onOpenCountryModal,
  onOpenPeriodModal,
}: AccountBookSettingsFormProps) => {
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

export { AccountBookConfigurator, ConfiguratorSkeleton };
