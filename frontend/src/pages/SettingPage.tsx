import { type ComponentPropsWithoutRef, useState } from 'react';
import clsx from 'clsx';

import Dropdown from '@/components/common/dropdown/Dropdown';
import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';

import { Icons } from '@/assets';

const CARD_MOCK_DATA = [
  {
    id: 1,
    cardImg: 'https://example.com/hana_viva_x.png', // 실제 이미지 경로로 대체
    cardName: '하나 비바X',
    cardNumber: '2468',
    nickname: '별명어쩌구',
  },
  {
    id: 2,
    cardImg: 'https://example.com/travel_wallet.png', // 실제 이미지 경로로 대체
    cardName: '트레블월렛',
    cardNumber: '8372',
    nickname: '별명어쩌구',
  },
];

const LEDGER_SETTINGS_MOCK = [
  {
    id: 1,
    tabName: '가계부 1',
    isMain: true,
    settings: {
      name: '가계부 1',
      baseCurrency: '원 ₩ KRW',
      country: '미국',
      syncPeriod: {
        start: '2025.12.23',
        end: '2026.05.31',
      },
    },
  },
  {
    id: 2,
    tabName: '가계부 2',
    isMain: false,
    settings: {
      name: '가계부 2',
      baseCurrency: '달러 $ USD',
      country: '미국',
      syncPeriod: {
        start: '2026.01.01',
        end: '2026.12.31',
      },
    },
  },
];

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

interface LinkedCardItemProps {
  cardImg: string;
  cardName: string;
  cardNumber: string;
  nickname: string;
}
const LinkedCardItem = ({
  cardImg,
  cardName,
  cardNumber,
  nickname,
}: LinkedCardItemProps) => {
  return (
    <div className="flex items-center justify-between border-b border-gray-100 py-3 last:border-0">
      {/* 1. 카드 정보 영역 (왼쪽) */}
      <div className="flex items-center gap-4">
        {/* 카드 이미지 (하나 비바X 등) */}
        <div className="h-10 w-16 overflow-hidden rounded-md border border-gray-200 bg-white">
          <img
            src={cardImg}
            alt={cardName}
            className="h-full w-full object-cover"
          />
        </div>

        {/* 카드 텍스트 정보 */}
        <div className="flex items-center gap-2 text-sm">
          <span className="font-semibold text-gray-900">{cardName}</span>
          <span className="text-gray-400">({cardNumber})</span>
          <span className="mx-1 text-gray-300">|</span>
          <span className="text-gray-500">{nickname}</span>
        </div>
      </div>

      {/* 2. 액션 버튼 영역 (오른쪽) */}
      <div className="flex items-center gap-2">
        <button className="rounded-full p-2 transition-colors hover:bg-gray-100">
          <Icons.Update className="h-5 w-5 text-gray-400" />
        </button>
        <button className="rounded-full p-2 text-red-400 transition-colors hover:bg-gray-100">
          <Icons.Trash className="h-5 w-5" />
        </button>
      </div>
    </div>
  );
};

const LinkedCardList = () => {
  return (
    <SettingSection>
      <SettingTitle>국내카드 연동 목록</SettingTitle>
      <div className="flex flex-col">
        {CARD_MOCK_DATA.map((card) => (
          <LinkedCardItem
            key={card.id}
            cardImg={card.cardImg}
            cardName={card.cardName}
            cardNumber={card.cardNumber}
            nickname={card.nickname}
          />
        ))}

        {/* 새 카드 추가 버튼 영역 */}
        <button className="flex items-center gap-4 py-3">
          <div className="flex h-10 w-16 items-center justify-center rounded-md border border-dashed border-gray-300 bg-gray-50">
            <span className="text-xl text-gray-400">+</span>
          </div>
          <span className="text-sm text-gray-500">새 카드 추가</span>
        </button>
      </div>
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
