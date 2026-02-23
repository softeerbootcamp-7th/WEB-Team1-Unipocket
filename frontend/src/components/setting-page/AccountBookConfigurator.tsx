import { useState } from 'react';

import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';
import AccountBookCreateModal from '@/components/setting-page/modal/AccountBookCreateModal';
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
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useGetAccountBooksQuery,
  useUpdateAccountBookMutation,
} from '@/api/account-books/query';
import type {
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
} from '@/api/account-books/type';
import { Icons } from '@/assets';
import type { CountryCode } from '@/data/country/countryCode';

import Chip from '../common/Chip';

const ConfiguratorSkeleton = () => (
  <div className="h-32 w-full animate-pulse rounded-md bg-black/10" />
);

const AccountBookConfigurator = () => {
  const { data: accountBooks } = useGetAccountBooksQuery();
  const createAccountBookMutation = useCreateAccountBookMutation();

  const [isCreateModalOpen, setCreateModalOpen] = useState(false);

  const handleCreateAccountBook = (data: {
    localCountryCode: CountryCode;
    startDate: string;
    endDate: string;
  }) => {
    createAccountBookMutation.mutate(data, {
      onSuccess: () => setCreateModalOpen(false),
    });
  };

  const [activeAccountBookId, setActiveAccountBookId] = useState(
    accountBooks[0].accountBookId.toString(),
  );

  const { data: accountBookDetail } = useAccountBookDetailQuery(
    Number(activeAccountBookId),
  );

  return (
    <SettingSection>
      <SettingTitle>가계부 설정</SettingTitle>
      <TabProvider
        value={activeAccountBookId}
        onValueChange={setActiveAccountBookId}
      >
        <div className="flex flex-wrap items-end gap-5">
          <TabList>
            {accountBooks.map((book) => (
              <TabTrigger
                key={book.accountBookId}
                value={String(book.accountBookId)}
              >
                <span className="headline1-bold text-label-normal flex min-w-0 items-center justify-center gap-3 px-3.5 pt-3">
                  <span className="min-w-25 truncate">{book.title}</span>
                  {book.isMain && (
                    <Chip
                      label="메인"
                      bgClassName="bg-primary-normal/8"
                      textClassName="text-primary-normal"
                    />
                  )}
                </span>
              </TabTrigger>
            ))}
          </TabList>
          <button
            onClick={() => setCreateModalOpen(true)}
            className="headline1-bold text-label-assistive flex items-center justify-center gap-3.5 px-2.5 pb-3.5"
          >
            <Icons.Add className="size-4" />새 가계부 추가
          </button>
        </div>

        <TabContent value={activeAccountBookId}>
          {accountBookDetail && (
            <AccountBookSettingsForm
              key={accountBookDetail.accountBookId}
              detail={accountBookDetail}
              accountBooks={accountBooks}
            />
          )}
        </TabContent>
      </TabProvider>

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

interface AccountBookSettingsFormProps {
  detail: GetAccountBookDetailResponse;
  accountBooks: GetAccountBooksResponse;
}

const AccountBookSettingsForm = ({
  detail,
  accountBooks,
}: AccountBookSettingsFormProps) => {
  const updateAccountBookMutation = useUpdateAccountBookMutation();
  const deleteAccountBookMutation = useDeleteAccountBookMutation();

  const [isNameModalOpen, setNameModalOpen] = useState(false);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [isPeriodModalOpen, setPeriodModalOpen] = useState(false);

  const isMain =
    accountBooks.find((book) => book.accountBookId === detail.accountBookId)
      ?.isMain ?? false;

  const formatDate = (dateStr: string) => dateStr.replace(/-/g, '.');
  const periodDisplay = `${formatDate(detail.startDate)} - ${formatDate(detail.endDate)}`;

  return (
    <>
      <div className="flex flex-col items-start gap-5">
        <div className="border-line-normal-normal flex flex-col items-start gap-2 border-y px-4 py-4.5">
          <SettingRow
            label="이름 수정"
            value={detail.title}
            onEdit={() => setNameModalOpen(true)}
          />
          <SettingRow
            label="가계부 기간 변경"
            value={periodDisplay}
            onEdit={() => setPeriodModalOpen(true)}
          />
          <button
            onClick={() => setDeleteModalOpen(true)}
            className="body1-normal-bold text-status-negative py-2.5 text-left"
          >
            {detail.title} 삭제
          </button>
        </div>

        <p className="body2-normal-regular text-label-assistive">
          * 한 가계부 안에, 지출 내역, 기준 통화 설정, 국가 설정, 가계부 기간
          정보가 포함되어 있어요.
          <br />* 두 번째 교환학생을 가거나 가계부를 분리하고 싶다면, 새로운
          가계부를 추가해주세요.
        </p>
      </div>

      {isNameModalOpen && (
        <AccountBookNameModal
          accountBooks={accountBooks}
          accountBookId={detail.accountBookId}
          currentTitle={detail.title}
          isSubmitting={updateAccountBookMutation.isPending}
          onClose={() => setNameModalOpen(false)}
          onSubmit={(title) => {
            updateAccountBookMutation.mutate(
              { accountBookId: detail.accountBookId, data: { title } },
              { onSuccess: () => setNameModalOpen(false) },
            );
          }}
        />
      )}

      {isDeleteModalOpen && (
        <AccountBookDeleteModal
          accountBookTitle={detail.title}
          isMain={isMain}
          isSubmitting={deleteAccountBookMutation.isPending}
          onClose={() => setDeleteModalOpen(false)}
          onConfirm={() => {
            deleteAccountBookMutation.mutate(detail.accountBookId, {
              onSuccess: () => setDeleteModalOpen(false),
            });
          }}
        />
      )}

      {isPeriodModalOpen && (
        <AccountBookPeriodModal
          currentStartDate={detail.startDate}
          currentEndDate={detail.endDate}
          isSubmitting={updateAccountBookMutation.isPending}
          onClose={() => setPeriodModalOpen(false)}
          onSubmit={(startDate, endDate) => {
            updateAccountBookMutation.mutate(
              {
                accountBookId: detail.accountBookId,
                data: { startDate, endDate },
              },
              { onSuccess: () => setPeriodModalOpen(false) },
            );
          }}
        />
      )}
    </>
  );
};

export { AccountBookConfigurator, ConfiguratorSkeleton };
