import { useState } from 'react';
import { useNavigate } from '@tanstack/react-router';

import Dropdown from '@/components/common/dropdown/Dropdown';
import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';

import {
  accountBookDetailQueryOptions,
  useGetAccountBooksQuery,
} from '@/api/account-books/query';
import { queryClient } from '@/main';
import { useAccountBookStore } from '@/stores/accountBookStore';
import { useParseSnackbarStore } from '@/stores/parseSnackbarStore';

const AccountBookSelector = () => {
  const { accountBook, setAccountBook } = useAccountBookStore();
  const { data } = useGetAccountBooksQuery();

  const snackbars = useParseSnackbarStore((s) => s.snackbars);
  const resetAll = useParseSnackbarStore((s) => s.resetAll);
  const [pendingId, setPendingId] = useState<number | null>(null);

  const navigate = useNavigate();

  if (!accountBook) {
    return null;
  }

  const accountBookOptions = data.map(({ accountBookId, title }) => ({
    id: accountBookId,
    name: title,
  }));

  const switchAccountBook = async (selectedId: number) => {
    const accountBookDetail = await queryClient.ensureQueryData(
      accountBookDetailQueryOptions(selectedId),
    );
    resetAll();
    setAccountBook(accountBookDetail);
    navigate({
      to: '/home',
      search: {},
    });
  };

  const handleOnSelect = async (selectedId: number) => {
    if (!selectedId || selectedId === accountBook.accountBookId) return;

    if (snackbars.length > 0) {
      setPendingId(selectedId);
      return;
    }

    await switchAccountBook(selectedId);
  };

  return (
    <>
      <Dropdown
        options={accountBookOptions}
        selectedId={accountBook.accountBookId}
        onSelect={handleOnSelect}
        itemWidth="w-60"
      />
      <TextConfirmModal
        isOpen={pendingId !== null}
        onClose={() => setPendingId(null)}
        onAction={async () => {
          if (pendingId !== null) {
            await switchAccountBook(pendingId);
            setPendingId(null);
          }
        }}
        title="가계부를 변경할까요?"
        description={`가계부를 바꾸면 현재 분석 중이거나\n분석 완료된 임시지출 내역은 사라져요.`}
        confirmButton={{ label: '변경하기', variant: 'danger' }}
      />
    </>
  );
};

export default AccountBookSelector;
