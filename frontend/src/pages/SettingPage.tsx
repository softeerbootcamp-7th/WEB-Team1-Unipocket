import { Suspense } from 'react';

import {
  AccountBookConfigurator,
  ConfiguratorSkeleton,
} from '@/components/setting-page/AccountBookConfigurator';
import { AccountManagement } from '@/components/setting-page/AccountManagement';
import {
  CardListSkeleton,
  LinkedCardList,
} from '@/components/setting-page/LinkedCardList';
import {
  MainAccountBookSelection,
  MainAccountBookSkeleton,
} from '@/components/setting-page/MainAccountBookSelection';

const SettingPage = () => {
  return (
    <div className="flex flex-1 flex-col bg-amber-100 px-35 py-7.5">
      <h1 className="title2-semibold text-label-normal mb-6.5">설정</h1>
      <div className="flex flex-1 flex-col gap-3.5 bg-amber-200">
        <Suspense fallback={<MainAccountBookSkeleton />}>
          <MainAccountBookSelection />
        </Suspense>
        <Suspense fallback={<CardListSkeleton />}>
          <LinkedCardList />
        </Suspense>
        <Suspense fallback={<ConfiguratorSkeleton />}>
          <AccountBookConfigurator />
        </Suspense>
        <AccountManagement />
      </div>
    </div>
  );
};

export default SettingPage;
