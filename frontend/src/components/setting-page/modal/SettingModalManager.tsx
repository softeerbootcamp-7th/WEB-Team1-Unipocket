import LocaleSelectModal from '@/components/modal/LocaleSelectModal/LocaleSelectModal';
import SelectDateModal from '@/components/modal/SelectDateModal';
import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';
import TextInputModal from '@/components/modal/TextModal/TextInputModal';
import { SETTING_MODAL_TEXT } from '@/components/setting-page/modal/messsage';
import type { SettingModalState } from '@/components/setting-page/modal/useSettingModal';

import {
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useUpdateAccountBookMutation,
} from '@/api/account-books/query';
import {
  useDeleteCardMutation,
  useUpdateCardNicknameMutation,
} from '@/api/cards/query';
import { useDeleteUserMutation } from '@/api/users/query';
import { ERROR_MESSAGE } from '@/constants/message';
import type { CountryCode } from '@/data/country/countryCode';
import { formatDateToString, parseStringToDate } from '@/lib/utils';

interface SettingModalManagerProps {
  activeModal: SettingModalState;
  closeModal: () => void;
  openCreateAccountBookDate: (localCountryCode: CountryCode) => void;
}

const SettingModalManager = ({
  activeModal,
  closeModal,
  openCreateAccountBookDate,
}: SettingModalManagerProps) => {
  const createAccountBookMutation = useCreateAccountBookMutation();
  const updateAccountBookMutation = useUpdateAccountBookMutation();
  const deleteAccountBookMutation = useDeleteAccountBookMutation();
  const updateCardNicknameMutation = useUpdateCardNicknameMutation();
  const deleteCardMutation = useDeleteCardMutation();
  const deleteUserMutation = useDeleteUserMutation();

  return (
    <>
      {/* ── TextInput 모달 ── */}

      {/* 카드 별명 수정 */}
      <TextInputModal
        isOpen={activeModal.type === 'EDIT_CARD_NICKNAME'}
        onClose={closeModal}
        initialValue={
          activeModal.type === 'EDIT_CARD_NICKNAME'
            ? activeModal.currentNickname
            : ''
        }
        {...SETTING_MODAL_TEXT.EDIT_CARD_NICKNAME}
        confirmButton={{
          label: SETTING_MODAL_TEXT.EDIT_CARD_NICKNAME.confirm,
          variant: 'solid',
        }}
        onAction={(nickname) => {
          if (activeModal.type === 'EDIT_CARD_NICKNAME') {
            updateCardNicknameMutation.mutate(
              { cardId: activeModal.cardId, data: { nickname } },
              { onSuccess: closeModal },
            );
          }
        }}
      />

      {/* 가계부 이름 수정 */}
      <TextInputModal
        isOpen={activeModal.type === 'EDIT_ACCOUNT_BOOK_NAME'}
        onClose={closeModal}
        initialValue={
          activeModal.type === 'EDIT_ACCOUNT_BOOK_NAME'
            ? activeModal.currentTitle
            : ''
        }
        {...SETTING_MODAL_TEXT.EDIT_ACCOUNT_BOOK_NAME}
        confirmButton={{
          label: SETTING_MODAL_TEXT.EDIT_ACCOUNT_BOOK_NAME.confirm,
          variant: 'solid',
        }}
        validate={(val) => {
          if (val.trim().length > 30) return ERROR_MESSAGE.LENGTH30;
          if (
            activeModal.type === 'EDIT_ACCOUNT_BOOK_NAME' &&
            activeModal.existingTitles.includes(val.trim())
          ) {
            return ERROR_MESSAGE.ACCOUNT_BOOK_NAME_DUPLICATE;
          }
          return undefined;
        }}
        onAction={(title) => {
          if (activeModal.type === 'EDIT_ACCOUNT_BOOK_NAME') {
            updateAccountBookMutation.mutate(
              { accountBookId: activeModal.accountBookId, data: { title } },
              { onSuccess: closeModal },
            );
          }
        }}
      />

      {/* ── TextContext(확인/삭제) 모달 ── */}

      {/* 카드 삭제 */}
      <TextConfirmModal
        isOpen={activeModal.type === 'DELETE_CARD'}
        onClose={closeModal}
        title={SETTING_MODAL_TEXT.DELETE_CARD.title}
        description={
          activeModal.type === 'DELETE_CARD'
            ? SETTING_MODAL_TEXT.DELETE_CARD.description(
                activeModal.cardNickname,
              )
            : ''
        }
        onAction={() => {
          if (activeModal.type === 'DELETE_CARD') {
            deleteCardMutation.mutate(activeModal.cardId, {
              onSuccess: closeModal,
            });
          }
        }}
      />

      {/* 가계부 삭제 */}
      <TextConfirmModal
        isOpen={activeModal.type === 'DELETE_ACCOUNT_BOOK'}
        onClose={closeModal}
        title={SETTING_MODAL_TEXT.DELETE_ACCOUNT_BOOK.title}
        description={
          activeModal.type === 'DELETE_ACCOUNT_BOOK'
            ? SETTING_MODAL_TEXT.DELETE_ACCOUNT_BOOK.description(
                activeModal.title,
              )
            : ''
        }
        subDescription={
          activeModal.type === 'DELETE_ACCOUNT_BOOK' && activeModal.isMain
            ? SETTING_MODAL_TEXT.DELETE_ACCOUNT_BOOK.subDescription
            : undefined
        }
        onAction={() => {
          if (activeModal.type === 'DELETE_ACCOUNT_BOOK') {
            deleteAccountBookMutation.mutate(activeModal.accountBookId, {
              onSuccess: closeModal,
            });
          }
        }}
      />

      {/* 계정 삭제 */}
      <TextConfirmModal
        isOpen={activeModal.type === 'DELETE_ACCOUNT'}
        onClose={closeModal}
        title={SETTING_MODAL_TEXT.DELETE_ACCOUNT.title}
        description={SETTING_MODAL_TEXT.DELETE_ACCOUNT.description}
        onAction={() => deleteUserMutation.mutate()}
      />

      {/* ── 기간 선택 모달 ── */}
      <SelectDateModal
        isOpen={activeModal.type === 'EDIT_ACCOUNT_BOOK_PERIOD'}
        onClose={closeModal}
        initialDateRange={
          activeModal.type === 'EDIT_ACCOUNT_BOOK_PERIOD'
            ? {
                startDate: parseStringToDate(activeModal.startDate),
                endDate: parseStringToDate(activeModal.endDate),
              }
            : undefined
        }
        onConfirm={(dateRange) => {
          if (
            activeModal.type === 'EDIT_ACCOUNT_BOOK_PERIOD' &&
            dateRange.startDate &&
            dateRange.endDate
          ) {
            updateAccountBookMutation.mutate(
              {
                accountBookId: activeModal.accountBookId,
                data: {
                  startDate: formatDateToString(dateRange.startDate),
                  endDate: formatDateToString(dateRange.endDate),
                },
              },
              { onSuccess: closeModal },
            );
          }
        }}
      />

      {/* ── 통화/국가 선택 모달 ── */}
      <LocaleSelectModal
        isOpen={
          activeModal.type === 'EDIT_BASE_CURRENCY' ||
          activeModal.type === 'EDIT_LOCAL_CURRENCY'
        }
        onClose={closeModal}
        mode={activeModal.type === 'EDIT_BASE_CURRENCY' ? 'BASE' : 'LOCAL'}
        baseCountryCode={
          activeModal.type === 'EDIT_BASE_CURRENCY' ||
          activeModal.type === 'EDIT_LOCAL_CURRENCY'
            ? activeModal.baseCountryCode
            : null
        }
        localCountryCode={
          activeModal.type === 'EDIT_BASE_CURRENCY' ||
          activeModal.type === 'EDIT_LOCAL_CURRENCY'
            ? activeModal.localCountryCode
            : null
        }
        onSelect={(code) => {
          if (
            activeModal.type === 'EDIT_BASE_CURRENCY' ||
            activeModal.type === 'EDIT_LOCAL_CURRENCY'
          ) {
            const data =
              activeModal.type === 'EDIT_BASE_CURRENCY'
                ? { baseCountryCode: code }
                : { localCountryCode: code };
            updateAccountBookMutation.mutate(
              { accountBookId: activeModal.accountBookId, data },
              { onSuccess: closeModal },
            );
          }
        }}
      />

      {/* ── 가계부 생성 모달 (국가 선택 → 기간 선택) ── */}
      <LocaleSelectModal
        isOpen={activeModal.type === 'CREATE_ACCOUNT_BOOK_LOCALE'}
        onClose={closeModal}
        mode="INIT"
        baseCountryCode={null}
        localCountryCode={null}
        onSelect={openCreateAccountBookDate}
      />

      <SelectDateModal
        isOpen={activeModal.type === 'CREATE_ACCOUNT_BOOK_DATE'}
        onClose={closeModal}
        onConfirm={(dateRange) => {
          if (
            activeModal.type === 'CREATE_ACCOUNT_BOOK_DATE' &&
            dateRange.startDate &&
            dateRange.endDate
          ) {
            createAccountBookMutation.mutate(
              {
                localCountryCode: activeModal.localCountryCode,
                startDate: formatDateToString(dateRange.startDate),
                endDate: formatDateToString(dateRange.endDate),
              },
              { onSuccess: closeModal },
            );
          }
        }}
      />
    </>
  );
};

export default SettingModalManager;
