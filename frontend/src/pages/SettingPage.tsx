import { Suspense } from 'react';
import { useRouter } from '@tanstack/react-router';

import Icon from '@/components/common/Icon';
import {
  AccountBookConfigurator,
  ConfiguratorSkeleton,
} from '@/components/setting-page/AccountBookConfigurator';
import { AccountManagement } from '@/components/setting-page/AccountManagement';
import {
  LinkedCardList,
  LinkedCardListSkeleton,
} from '@/components/setting-page/LinkedCardList';
import {
  MainAccountBookSelector,
  MainAccountBookSkeleton,
} from '@/components/setting-page/MainAccountBookSelector';
import SettingModalManager from '@/components/setting-page/modal/SettingModalManager';
import { useSettingModal } from '@/components/setting-page/modal/useSettingModal';

const SettingPage = () => {
  const router = useRouter();
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
    openCreateAccountBook,
    openCreateAccountBookDate,
  } = useSettingModal();

  return (
    <div className="flex flex-1 flex-col gap-6.5 px-35 py-7.5">
      <div className="flex items-center gap-2.5">
        <Icon
          iconName="ChevronBack"
          onClick={() => router.history.back()}
          color="text-label-normal"
        />
        <h1 className="title2-semibold text-label-normal">설정</h1>
      </div>
      <div className="flex flex-1 flex-col gap-3.5">
        <Suspense fallback={<MainAccountBookSkeleton />}>
          <MainAccountBookSelector />
        </Suspense>
        <Suspense fallback={<LinkedCardListSkeleton />}>
          <LinkedCardList
            openEditCardNickname={openEditCardNickname}
            openDeleteCard={openDeleteCard}
          />
        </Suspense>
        <Suspense fallback={<ConfiguratorSkeleton />}>
          <AccountBookConfigurator
            openEditAccountBookName={openEditAccountBookName}
            openDeleteAccountBook={openDeleteAccountBook}
            openEditAccountBookPeriod={openEditAccountBookPeriod}
            openEditBaseCurrency={openEditBaseCurrency}
            openEditLocalCurrency={openEditLocalCurrency}
            openCreateAccountBook={openCreateAccountBook}
          />
        </Suspense>
        <AccountManagement openDeleteAccount={openDeleteAccount} />
      </div>

      <SettingModalManager
        activeModal={activeModal}
        closeModal={closeModal}
        openCreateAccountBookDate={openCreateAccountBookDate}
      />
    </div>
  );
};

export default SettingPage;
