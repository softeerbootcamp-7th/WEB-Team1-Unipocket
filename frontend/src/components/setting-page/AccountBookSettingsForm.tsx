import { useState } from 'react';

import AccountBookDeleteModal from '@/components/setting-page/modal/AccountBookDeleteModal';
import AccountBookNameModal from '@/components/setting-page/modal/AccountBookNameModal';
import AccountBookPeriodModal from '@/components/setting-page/modal/AccountBookPeriodModal';
import { SettingRow } from '@/components/setting-page/SettingLayout';

import {
  useDeleteAccountBookMutation,
  useUpdateAccountBookMutation,
} from '@/api/account-books/query';
import type {
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
} from '@/api/account-books/type';
import { getCountryInfo } from '@/lib/country';

interface AccountBookSettingsFormProps {
  detail: GetAccountBookDetailResponse;
  accountBooks: GetAccountBooksResponse;
}

const AccountBookSettingsForm = ({
  detail,
  accountBooks,
}: AccountBookSettingsFormProps) => {
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

  const baseCountryInfo = getCountryInfo(detail.baseCountryCode);
  const localCountryInfo = getCountryInfo(detail.localCountryCode);

  const baseCurrencyDisplay = baseCountryInfo
    ? `${baseCountryInfo.currencyNameKor} (${baseCountryInfo.code})`
    : detail.baseCountryCode;
  const localCountryDisplay = localCountryInfo
    ? `${localCountryInfo.countryName} · ${localCountryInfo.currencyNameKor}`
    : detail.localCountryCode;

  return (
    <>
      <div className="flex flex-col items-start gap-5">
        <div className="border-line-normal-normal flex flex-col items-start gap-2 border-y px-4 py-4.5">
          <SettingRow
            label="이름 수정"
            value={detail.title}
            onEdit={() => setNameModalOpen(true)}
          />
          <SettingRow
            label="기준 통화 변경"
            value={baseCurrencyDisplay}
            onEdit={() => {}}
          />
          <SettingRow
            label="국가/통화 변경"
            value={localCountryDisplay}
            onEdit={() => {}}
          />
          <SettingRow
            label="가계부 기간 변경"
            value={periodDisplay}
            onEdit={() => setPeriodModalOpen(true)}
          />
          <button
            onClick={() => setDeleteModalOpen(true)}
            className="body1-normal-bold text-status-negative py-2.5 text-left"
          >
            {detail.title} 삭제
          </button>
        </div>

        <p className="body2-normal-regular text-label-assistive">
          * 한 가계부 안에, 지출 내역, 기준 통화 설정, 국가 설정, 가계부 기간
          정보가 포함되어 있어요.
          <br />* 두 번째 교환학생을 가거나 가계부를 분리하고 싶다면, 새로운
          가계부를 추가해주세요.
        </p>
      </div>

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

export { AccountBookSettingsForm };
export type { AccountBookSettingsFormProps };
