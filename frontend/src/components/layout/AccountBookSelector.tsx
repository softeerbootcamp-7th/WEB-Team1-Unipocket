import { useNavigate } from '@tanstack/react-router';

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
  const navigate = useNavigate();

  if (!accountBook) {
    return null;
  }

  const accountBookOptions = data.map(({ accountBookId, title }) => ({
    id: accountBookId,
    name: title,
  }));

  const handleOnSelect = async (selectedId: number) => {
    if (!selectedId) return;

    const accountBookDetail = await queryClient.ensureQueryData(
      accountBookDetailQueryOptions(selectedId),
    );

    setAccountBook(accountBookDetail);
    navigate({ to: '/home' });
  };

  return (
    <Dropdown
      options={accountBookOptions}
      selectedId={accountBook.accountBookId}
      onSelect={handleOnSelect}
      itemWidth="w-60"
    />
  );
};

export default AccountBookSelector;
