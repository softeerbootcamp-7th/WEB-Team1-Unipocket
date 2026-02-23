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

const SettingPage = () => {
  return (
    <div className="flex flex-1 flex-col gap-6.5 px-35 py-7.5">
      <h1 className="title2-semibold text-label-normal">설정</h1>
      <div className="flex flex-1 flex-col gap-3.5">
        <Suspense fallback={<MainAccountBookSkeleton />}>
          <MainAccountBookSelector />
        </Suspense>
        <LinkedCardList />
        <Suspense fallback={<ConfiguratorSkeleton />}>
          <AccountBookConfigurator />
        </Suspense>
        <AccountManagement />
      </div>
    </div>
  );
};

export default SettingPage;
