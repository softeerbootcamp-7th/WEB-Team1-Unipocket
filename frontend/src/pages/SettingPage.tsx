import { Suspense } from 'react';

import {
  AccountBookConfigurator,
  ConfiguratorSkeleton,
} from '@/components/setting-page/AccountBookConfigurator';
import { AccountManagement } from '@/components/setting-page/AccountManagement';
import { LinkedCardList } from '@/components/setting-page/LinkedCardList';
import {
  MainAccountBookSelector,
  MainAccountBookSkeleton,
} from '@/components/setting-page/MainAccountBookSelector';
import SettingModalManager from '@/components/setting-page/modal/SettingModalManager';
import { useSettingModal } from '@/components/setting-page/modal/useSettingModal';

const SettingPage = () => {
  const {
    activeModal,
    closeModal,
    openEditCardNickname,
    openDeleteCard,
    openEditAccountBookName,
    openDeleteAccountBook,
    openDeleteAccount,
    openEditAccountBookPeriod,
    openEditBaseCurrency,
    openEditLocalCurrency,
  } = useSettingModal();

  return (
    <div className="flex flex-1 flex-col gap-6.5 px-35 py-7.5">
      <h1 className="title2-semibold text-label-normal">설정</h1>
      <div className="flex flex-1 flex-col gap-3.5">
        <Suspense fallback={<MainAccountBookSkeleton />}>
          <MainAccountBookSelector />
        </Suspense>
        <LinkedCardList
          openEditCardNickname={openEditCardNickname}
          openDeleteCard={openDeleteCard}
        />
        <Suspense fallback={<ConfiguratorSkeleton />}>
          <AccountBookConfigurator
            openEditAccountBookName={openEditAccountBookName}
            openDeleteAccountBook={openDeleteAccountBook}
            openEditAccountBookPeriod={openEditAccountBookPeriod}
            openEditBaseCurrency={openEditBaseCurrency}
            openEditLocalCurrency={openEditLocalCurrency}
          />
        </Suspense>
        <AccountManagement openDeleteAccount={openDeleteAccount} />
      </div>

      <SettingModalManager activeModal={activeModal} closeModal={closeModal} />
    </div>
  );
};

export default SettingPage;
