import Dropdown from '@/components/common/dropdown/Dropdown';

import {
  accountBookDetailQueryOptions,
  useGetAccountBooksQuery,
} from '@/api/account-books/query';
import { queryClient } from '@/main';
import { useAccountBookStore } from '@/stores/accountBookStore';

const AccountBookSelector = () => {
  const { accountBook, setAccountBook } = useAccountBookStore();
  const { data } = useGetAccountBooksQuery();

  if (!accountBook) {
    return null;
  }

  const accountBookOptions = data.map(({ id, title }) => ({
    id,
    name: title,
  }));

  const handleOnSelect = async (selectedId: number) => {
    if (!selectedId) return;

    const accountBookDetail = await queryClient.ensureQueryData(
      accountBookDetailQueryOptions(selectedId),
    );

    setAccountBook(accountBookDetail);
  };

  return (
    <Dropdown
      options={accountBookOptions}
      selectedId={accountBook.id}
      onSelect={handleOnSelect}
      itemWidth="w-fit"
    />
  );
};

export default AccountBookSelector;
