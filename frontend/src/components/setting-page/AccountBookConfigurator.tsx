import { useState } from 'react';

import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';
import {
  AccountBookSettingsForm,
  type AccountBookSettingsFormProps,
} from '@/components/setting-page/AccountBookSettingsForm';
import {
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

import {
  useAccountBookDetailQuery,
  useGetAccountBooksQuery,
} from '@/api/account-books/query';
import { Icons } from '@/assets';

const ConfiguratorSkeleton = () => (
  <div className="h-32 w-full animate-pulse rounded-md bg-black/10" />
);

type AccountBookModalOpeners = Pick<
  AccountBookSettingsFormProps,
  | 'openEditAccountBookName'
  | 'openDeleteAccountBook'
  | 'openEditAccountBookPeriod'
  | 'openEditBaseCurrency'
  | 'openEditLocalCurrency'
> & {
  openCreateAccountBook: () => void;
};

const AccountBookConfigurator = ({
  openEditAccountBookName,
  openDeleteAccountBook,
  openEditAccountBookPeriod,
  openEditBaseCurrency,
  openEditLocalCurrency,
  openCreateAccountBook,
}: AccountBookModalOpeners) => {
  const { data: accountBooks } = useGetAccountBooksQuery();

  const [activeAccountBookId, setActiveAccountBookId] = useState(
    accountBooks[0].accountBookId.toString(),
  );

  // 렌더 중 동기적으로 유효한 ID 계산 (삭제된 ID로 상세 조회 방지)
  const activeIdExists = accountBooks.some(
    (book) => String(book.accountBookId) === activeAccountBookId,
  );
  const resolvedActiveId = activeIdExists
    ? activeAccountBookId
    : accountBooks[0].accountBookId.toString();

  const { data: accountBookDetail } = useAccountBookDetailQuery(
    Number(resolvedActiveId),
  );

  return (
    <SettingSection>
      <SettingTitle>가계부 설정</SettingTitle>
      <TabProvider
        value={resolvedActiveId}
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
                </span>
              </TabTrigger>
            ))}
          </TabList>
          <button
            onClick={openCreateAccountBook}
            className="headline1-bold text-label-assistive flex items-center justify-center gap-3.5 px-2.5 pb-3.5"
          >
            <Icons.Add className="size-4" />새 가계부 추가
          </button>
        </div>

        <TabContent value={resolvedActiveId}>
          {accountBookDetail && (
            <AccountBookSettingsForm
              key={accountBookDetail.accountBookId}
              detail={accountBookDetail}
              accountBooks={accountBooks}
              openEditAccountBookName={openEditAccountBookName}
              openDeleteAccountBook={openDeleteAccountBook}
              openEditAccountBookPeriod={openEditAccountBookPeriod}
              openEditBaseCurrency={openEditBaseCurrency}
              openEditLocalCurrency={openEditLocalCurrency}
            />
          )}
        </TabContent>
      </TabProvider>
    </SettingSection>
  );
};

export { AccountBookConfigurator, ConfiguratorSkeleton };
