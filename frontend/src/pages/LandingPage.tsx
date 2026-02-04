import { Link } from '@tanstack/react-router';

import Button from '@/components/common/Button';
import { DataTable } from '@/components/common/data-table/DataTable';
import DataTableProvider from '@/components/common/data-table/DataTableProvider';
import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';
import UploadBox from '@/components/common/upload/UploadBox';
import { columns } from '@/components/landing-page/columns';
import { type Expense, getData } from '@/components/landing-page/dummy';
import FeatureCard from '@/components/landing-page/FeatureCard';
import InfiniteCurrency from '@/components/landing-page/InfinityCurrency';

import { LandingImages } from '@/assets';

const LandingPage = () => {
  return (
    <main className="-mt-14.25">
      <HomeSection />
      <FeatureSection />
      <DemoSection />
    </main>
  );
};

export default LandingPage;

const HomeSection = () => {
  return (
    <section
      id="home"
      className="flex flex-col items-center gap-10 px-10 py-35 lg:flex-row lg:items-center lg:justify-between lg:px-60 lg:py-42.5"
    >
      <div className="title1-bold flex flex-col lg:block">
        <div className="mb-4 flex flex-col items-center md:items-start">
          <h1 className="text-primary-normal text-center md:text-start">
            교환학생<span className="text-label-normal">의</span>
          </h1>
          <h1 className="text-center md:text-start">모든 카드 지출을</h1>
          <div className="flex items-center gap-2">
            <span className="h-0.75 w-33.75 bg-black" />
            <h1 className="text-center md:text-start">하나로</h1>
          </div>
        </div>
        <h3 className="body1-normal-medium text-label-neutral mb-8 truncate text-center">
          교환학생 지출의 모든 것, 통합 가계부 유니포켓
        </h3>
        <div className="flex justify-center gap-2.5 md:justify-start">
          <Link to="/login">
            <Button variant="solid" size="lg">
              로그인
            </Button>
          </Link>
          <Link to="/" hash="demo">
            <Button variant="outlined" size="lg">
              데모 체험하기
            </Button>
          </Link>
        </div>
      </div>
      <img
        src={LandingImages.HomePreview}
        alt="Landing Preview"
        className="w-200"
      />
    </section>
  );
};

const FeatureSection = () => {
  return (
    <section id="features" className="flex flex-col p-10 lg:px-60 lg:py-45">
      <h2 className="title1-medium mb-6 text-center">
        교환학생 해외 지출을 어떻게 정리해야 할지 고민되나요?
      </h2>
      <h2 className="text-primary-normal mb-27.5 text-center text-3xl font-medium">
        유니포켓에서 쉽게 시작해봐요!
      </h2>
      <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
        <FeatureCard
          index={1}
          title={
            <span className="title3-semibold">
              <span className="text-primary-normal">이미지/CSV</span>로
              업로드하여
              <br />
              지출 내역 한번에 업로드 가능!
            </span>
          }
        >
          <img
            src={LandingImages.FeaturePreview1}
            alt="Preview 1"
            className="animate-float absolute right-12.25 bottom-10 w-40 object-contain lg:w-67.5"
          />
        </FeatureCard>

        <FeatureCard
          index={2}
          title={
            <span className="title3-semibold">
              소비 시점의 <span className="text-primary-normal">환율</span>로
              자동 계산!
              <br />
              여러 통화도 Okay!
            </span>
          }
        >
          <div className="absolute bottom-20">
            <InfiniteCurrency />
          </div>
        </FeatureCard>

        <FeatureCard
          index={3}
          title={
            <span className="title3-semibold">
              <span className="text-primary-normal">소비 분석 리포트</span>를
              통해
              <br />
              한눈에 소비 행태 확인 가능!
            </span>
          }
        >
          <img
            src={LandingImages.FeaturePreview3}
            alt="Preview 1"
            className="absolute -bottom-35.25 left-50 w-163.5 object-contain lg:h-104"
          />
        </FeatureCard>
        <FeatureCard
          index={4}
          title={
            <span className="title3-semibold">
              교환학생의 꽃인
              <span className="text-primary-normal"> 여행</span>,
              <br />
              관련 지출 분석 별도 제공!
            </span>
          }
        >
          <img
            src={LandingImages.FeaturePreview4}
            alt="Preview 1"
            className="animate-float absolute right-12.25 bottom-10 w-30 object-contain lg:w-47.5"
          />
        </FeatureCard>
      </div>
    </section>
  );
};

const DemoSection = () => {
  const data = getData();
  return (
    <section id="demo" className="flex flex-col p-10 lg:px-60 lg:py-32.75">
      <h2 className="title1-medium mb-4 text-center">
        지출 내역을 올려보고 기능을 체험해보세요!
      </h2>
      <h2 className="text-label-alternative mb-20 text-center text-2xl font-medium">
        영수증 및 모바일 결제 화면 캡처본을 업로드하면
        <br /> 자동으로 내역이 정리됩니다.
      </h2>

      <div className="bg-background-normal shadow-card flex flex-col gap-12 rounded-3xl p-3 pb-7">
        <UploadBox />
        <div className="px-4.25">
          <TabProvider variant="underline" defaultValue="sample1">
            <TabList>
              <TabTrigger value="sample1">Sample1.png</TabTrigger>
              <TabTrigger value="sample2">Sample2.png</TabTrigger>
            </TabList>
            <div className="h-10" />
            <TabContent value="sample1">
              <div className="flex flex-col gap-4.5 lg:flex-row">
                <div className="bg-background-alternative flex items-center justify-center rounded-2xl border border-gray-200 p-2.5">
                  <img
                    src={LandingImages.DemoReceipt1}
                    className="h-120 rounded-lg"
                  />
                </div>
                <div className="shadow-card h-fit min-w-0 flex-1 rounded-2xl px-2 py-4">
                  <DataTableProvider columns={columns} data={data}>
                    <DataTable
                      height={480}
                      enableGroupSelection={false}
                      groupBy={(row: Expense) =>
                        new Date(row.date).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: '2-digit',
                          day: '2-digit',
                        })
                      }
                    />
                  </DataTableProvider>
                </div>
              </div>
            </TabContent>
            <TabContent value="sample2">
              <div className="flex flex-col gap-4.5 lg:flex-row">
                <div className="bg-background-alternative flex items-center justify-center rounded-2xl border border-gray-200 p-2.5">
                  <img
                    src={LandingImages.DemoReceipt1}
                    className="h-120 rounded-lg"
                  />
                </div>
                <div className="shadow-card h-fit min-w-0 flex-1 rounded-2xl px-2 py-4">
                  <DataTableProvider columns={columns} data={data}>
                    <DataTable
                      height={480}
                      enableGroupSelection={false}
                      groupBy={(row: Expense) =>
                        new Date(row.date).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: '2-digit',
                          day: '2-digit',
                        })
                      }
                    />
                  </DataTableProvider>
                </div>
              </div>
            </TabContent>
          </TabProvider>
        </div>
      </div>
    </section>
  );
};
