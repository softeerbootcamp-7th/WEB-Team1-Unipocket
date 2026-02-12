import { type ComponentPropsWithoutRef, useState } from 'react';
import clsx from 'clsx';

import Dropdown from '@/components/common/dropdown/Dropdown';
import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';

const SettingPage = () => {
  return (
    <div className="bg flex flex-1 flex-col px-30 py-8">
      <h1 className="title2-semibold text-label-normal mb-6.5">설정</h1>
      <div className="flex flex-col gap-3.5">
        <MainAccountBookSelection />
        <LinkedCardList />
        <AccountBookConfigurator />
        <AccountManagement />
      </div>
    </div>
  );
};

export default SettingPage;

const MainAccountBookSelection = () => {
  const [selectedId, setSelectedId] = useState<number | null>(null);

  // @TODO: 추후 options API 연동
  const options = [
    { id: 1, name: '미국 교환학생' },
    { id: 2, name: '2025 캐나다' },
    { id: 3, name: '독일 교환학생' },
  ];

  return (
    <SettingSection>
      <SettingTitle>메인 가계부 설정</SettingTitle>
      <div>
        <Dropdown
          selected={selectedId}
          onSelect={setSelectedId}
          options={options}
        />
      </div>
    </SettingSection>
  );
};
const LinkedCardList = () => {
  return (
    <SettingSection>
      <SettingTitle>국내카드 연동 목록</SettingTitle>
      <div>ss</div>
    </SettingSection>
  );
};
const AccountBookConfigurator = () => {
  return (
    <SettingSection>
      <SettingTitle>가계부 설정</SettingTitle>
      <TabProvider variant="underline" defaultValue="sample1">
        <TabList>
          <TabTrigger value="sample1">Sample1.png</TabTrigger>
        </TabList>
        <div className="h-10" />
        <TabContent value="sample1">
          <div className="bg-amber-300">ss</div>
        </TabContent>
      </TabProvider>
    </SettingSection>
  );
};
const AccountManagement = () => {
  return (
    <SettingSection>
      <SettingTitle disabled>계정 삭제</SettingTitle>
    </SettingSection>
  );
};

const SettingSection = ({ children }: { children: React.ReactNode }) => {
  return <div className="flex py-2.5">{children}</div>;
};

interface SettingTitleProps extends ComponentPropsWithoutRef<'h1'> {
  disabled?: boolean;
}

const SettingTitle = ({ children, disabled = false }: SettingTitleProps) => {
  return (
    <h1
      className={clsx('heading2-bold w-50', {
        'text-label-assistive': disabled,
        'text-label-normal': !disabled,
      })}
    >
      {children}
    </h1>
  );
};
