import Dropdown from '@/components/common/dropdown/Dropdown';

import {
  accountBookDetailQueryOptions,
  useGetAccountBooksQuery,
} from '@/api/account-books/query';
import { queryClient } from '@/main';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

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
    const selectedAccountBook = data.find((book) => book.id === selectedId);
    if (!accountBook) {
      const accountBookDetail = await queryClient.ensureQueryData(
        accountBookDetailQueryOptions(selectedAccountBook!.id),
      );

      setAccountBook(accountBookDetail);
    }
  };

  return (
    <Dropdown
      options={accountBookOptions}
      selectedId={accountBook.id}
      onSelect={handleOnSelect}
    />
  );
};

export default AccountBookSelector;
