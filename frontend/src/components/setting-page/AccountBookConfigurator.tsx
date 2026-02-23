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
import type { CountryCode } from '@/data/country/countryCode';

const ConfiguratorSkeleton = () => (
  <div className="h-32 w-full animate-pulse rounded-md bg-black/10" />
);

const AccountBookConfigurator = () => {
  const { data: accountBooks } = useGetAccountBooksQuery();
  const createAccountBookMutation = useCreateAccountBookMutation();

  //  부모는 '새 가계부 만들기' 모달 상태만 관리합니다.
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

  //  Suspense 쿼리이므로 isLoading 체크가 필요 없습니다.
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
        <div className="border-line-normal-neutral flex flex-wrap items-end gap-4 border-b">
          <TabList>
            {accountBooks.map((book) => (
              <TabTrigger
                key={book.accountBookId}
                value={String(book.accountBookId)}
              >
                <span className="body2-normal-medium flex items-center gap-1">
                  {book.title}
                  {book.isMain && (
                    <span className="caption1-regular bg-primary-normal rounded-full px-1.5 py-0.5 text-white">
                      메인
                    </span>
                  )}
                </span>
              </TabTrigger>
            ))}
          </TabList>
          <button
            onClick={() => setCreateModalOpen(true)}
            className="body2-normal-regular text-label-assistive pb-3.5"
          >
            + 새 가계부 추가
          </button>
        </div>

        <TabContent value={activeAccountBookId}>
          {/*  중복 렌더링되던 부분을 지우고, 자식에게 필요한 데이터를 모두 넘겨줍니다. */}
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

//  Props 타입 정의: 모달을 여는 함수 대신 필요한 데이터를 받습니다.
interface AccountBookSettingsFormProps {
  detail: GetAccountBookDetailResponse;
  accountBooks: GetAccountBooksResponse;
}

const AccountBookSettingsForm = ({
  detail,
  accountBooks,
}: AccountBookSettingsFormProps) => {
  //  자식 컴포넌트가 자신의 뮤테이션과 모달 상태를 스스로 관리합니다.
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
      <div className="flex flex-col">
        <SettingRow
          label="이름 수정"
          value={detail.title}
          onEdit={() => setNameModalOpen(true)}
        />
        {/* <SettingRow
          label="기준 통화 변경"
          value={currencyDisplay}
          onEdit={() => setCurrencyModalOpen(true)}
        />
        <SettingRow
          label="국가/통화 변경"
          value={countryDisplay}
          onEdit={() => setCountryModalOpen(true)}
        /> */}
        <SettingRow
          label="카드 연동 기간 변경"
          value={periodDisplay}
          onEdit={() => setPeriodModalOpen(true)}
        />
        <button
          onClick={() => setDeleteModalOpen(true)}
          className="body2-normal-regular text-status-negative py-3 text-left"
        >
          {detail.title} 삭제
        </button>
        <p className="caption1-regular text-label-assistive mt-2">
          * 한 가계부 안에, 지출 내역, 기준 통화 설정, 국가 설정, 카드 연동 기간
          정보가 포함되어 있어요.
        </p>
        <p className="caption1-regular text-label-assistive">
          * 두 번째 교환학생을 가거나 가계부를 분리하고 싶다면, 새로운 가계부를
          추가해주세요.
        </p>
      </div>

      {/*  개별 설정 모달들이 Form 컴포넌트 안에 위치합니다. */}
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
      {/* 
      {isCurrencyModalOpen && (
        <LocaleSelectModal
          mode="BASE"
          onSelect={handleBaseCountrySelect}
          selectedCode={selectedBaseCountry}
        />
      )}

      {isCountryModalOpen && (
        <LocaleSelectModal
          mode="LOCAL"
          onSelect={handleCountrySelect}
          selectedCode={selectedCountry}
        />
      )} */}

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
