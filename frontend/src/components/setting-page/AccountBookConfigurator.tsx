import { useState } from 'react';

import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';
import { AccountBookSettingsForm } from '@/components/setting-page/AccountBookSettingsForm';
import AccountBookCreateModal from '@/components/setting-page/modal/AccountBookCreateModal';
import {
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

import {
  useAccountBookDetailQuery,
  useCreateAccountBookMutation,
  useGetAccountBooksQuery,
} from '@/api/account-books/query';
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

export { AccountBookConfigurator, ConfiguratorSkeleton };
