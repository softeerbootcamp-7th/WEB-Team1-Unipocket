import { useState } from 'react';

import type { CountryCode } from '@/data/country/countryCode';

export type SettingModalState =
  | { type: 'NONE' }
  | { type: 'EDIT_CARD_NICKNAME'; cardId: number; currentNickname: string }
  | { type: 'DELETE_CARD'; cardId: number; cardNickname: string }
  | {
      type: 'EDIT_ACCOUNT_BOOK_NAME';
      accountBookId: number;
      currentTitle: string;
      existingTitles: string[];
    }
  | {
      type: 'DELETE_ACCOUNT_BOOK';
      accountBookId: number;
      title: string;
      isMain: boolean;
    }
  | { type: 'DELETE_ACCOUNT' }
  | {
      type: 'EDIT_ACCOUNT_BOOK_PERIOD';
      accountBookId: number;
      startDate: string;
      endDate: string;
    }
  | {
      type: 'EDIT_BASE_CURRENCY';
      accountBookId: number;
      baseCountryCode: CountryCode;
      localCountryCode: CountryCode;
    }
  | {
      type: 'EDIT_LOCAL_CURRENCY';
      accountBookId: number;
      baseCountryCode: CountryCode;
      localCountryCode: CountryCode;
    };

export const useSettingModal = () => {
  const [activeModal, setActiveModal] = useState<SettingModalState>({
    type: 'NONE',
  });

  const closeModal = () => setActiveModal({ type: 'NONE' });

  return {
    activeModal,
    closeModal,
    openEditCardNickname: (cardId: number, currentNickname: string) => {
      setActiveModal({ type: 'EDIT_CARD_NICKNAME', cardId, currentNickname });
    },
    openDeleteCard: (cardId: number, cardNickname: string) => {
      setActiveModal({ type: 'DELETE_CARD', cardId, cardNickname });
    },
    openEditAccountBookName: (
      accountBookId: number,
      currentTitle: string,
      existingTitles: string[],
    ) => {
      setActiveModal({
        type: 'EDIT_ACCOUNT_BOOK_NAME',
        accountBookId,
        currentTitle,
        existingTitles,
      });
    },
    openDeleteAccountBook: (
      accountBookId: number,
      title: string,
      isMain: boolean,
    ) => {
      setActiveModal({
        type: 'DELETE_ACCOUNT_BOOK',
        accountBookId,
        title,
        isMain,
      });
    },
    openDeleteAccount: () => {
      setActiveModal({ type: 'DELETE_ACCOUNT' });
    },
    openEditAccountBookPeriod: (
      accountBookId: number,
      startDate: string,
      endDate: string,
    ) => {
      setActiveModal({
        type: 'EDIT_ACCOUNT_BOOK_PERIOD',
        accountBookId,
        startDate,
        endDate,
      });
    },
    openEditBaseCurrency: (
      accountBookId: number,
      baseCountryCode: CountryCode,
      localCountryCode: CountryCode,
    ) => {
      setActiveModal({
        type: 'EDIT_BASE_CURRENCY',
        accountBookId,
        baseCountryCode,
        localCountryCode,
      });
    },
    openEditLocalCurrency: (
      accountBookId: number,
      baseCountryCode: CountryCode,
      localCountryCode: CountryCode,
    ) => {
      setActiveModal({
        type: 'EDIT_LOCAL_CURRENCY',
        accountBookId,
        baseCountryCode,
        localCountryCode,
      });
    },
  };
};
