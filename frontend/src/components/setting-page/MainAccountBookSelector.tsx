import Dropdown from '@/components/common/dropdown/Dropdown';
import { SettingSection } from '@/components/setting-page/SettingLayout';

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

  // 가계부가 하나도 없는 경우는 accountBookOptions가 0일 때 대체 텍스트 보여주도록 처리해서 0이 무의미함
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
      <p className="heading2-bold text-label-normal w-50 shrink-0">
        메인 가계부 설정
      </p>
      {accountBookOptions.length === 0 ? (
        <p className="body2-normal-regular text-label-assistive">
          아직 가계부가 없어요.
        </p>
      ) : (
        <div className="rounded-modal-8 bg-background-normal">
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

const MainAccountBookSkeleton = () => (
  <SettingSection>
    <p className="heading2-bold text-label-normal w-50">메인 가계부 설정</p>
    <Dropdown
      selectedId={0}
      onSelect={() => {}}
      options={[{ id: 0, name: '-' }]}
    />
  </SettingSection>
);

export { MainAccountBookSelector, MainAccountBookSkeleton };
