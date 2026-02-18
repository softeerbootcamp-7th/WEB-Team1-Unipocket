import Dropdown from '@/components/common/dropdown/Dropdown';

import { useGetAccountBooksQuery } from '@/api/account-books/query';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const AccountBookSelector = () => {
  const { accountBook, setAccountBook } = useAccountBookStore();
  const { data } = useGetAccountBooksQuery();

  if (!accountBook) {
    return null;
  }

  const accountBookOptions = data.map((book) => ({
    ...book,
    name: book.title,
  }));

  const handleOnSelect = (selectedId: number) => {
    const selectedAccountBook = data.find((book) => book.id === selectedId);
    if (selectedAccountBook) {
      setAccountBook(selectedAccountBook);
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
