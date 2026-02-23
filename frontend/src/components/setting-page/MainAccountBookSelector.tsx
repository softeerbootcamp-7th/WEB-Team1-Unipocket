import Dropdown from '@/components/common/dropdown/Dropdown';
import {
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

import {
  useGetAccountBooksQuery,
  useUpdateAccountBookMutation,
} from '@/api/account-books/query';

const MainAccountBookSelector = () => {
  const { data: accountBooks } = useGetAccountBooksQuery();
  const updateAccountBookMutation = useUpdateAccountBookMutation();

  const accountBookOptions = accountBooks.map((book) => ({
    id: book.accountBookId,
    name: book.title,
  }));

  const selectedId =
    accountBooks.find((book) => book.isMain)?.accountBookId ?? 0;

  const handleSelect = (id: number) => {
    if (id === selectedId) return;
    updateAccountBookMutation.mutate({
      accountBookId: id,
      data: { isMain: true },
    });
  };

  return (
    <SettingSection>
      <SettingTitle>메인 가계부 설정</SettingTitle>
      {accountBookOptions.length === 0 ? (
        <p className="body2-normal-regular text-label-assistive">
          아직 가계부가 없어요.
        </p>
      ) : (
        <div className="rounded-modal-8 bg-background-normal min-w-25">
          <Dropdown
            size="md"
            itemWidth="w-60"
            selectedId={selectedId}
            onSelect={handleSelect}
            options={accountBookOptions}
          />
        </div>
      )}
    </SettingSection>
  );
};

const MainAccountBookSkeleton = () => {
  return (
    <SettingSection>
      <SettingTitle>메인 가계부 설정</SettingTitle>
      <Dropdown
        selectedId={0}
        onSelect={() => {}}
        options={[{ id: 0, name: '-' }]}
      />
    </SettingSection>
  );
};

export { MainAccountBookSelector, MainAccountBookSkeleton };
