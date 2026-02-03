import { useEffect } from 'react';
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
import FeatureCard from '@/components/landing-page/FeatureCard';
import InfiniteCurrency from '@/components/landing-page/InfinityCurrency';

import { Icons } from '@/assets';
import DemoReceipt1 from '@/assets/images/landing/demo-receipt.png';
import DemoReceipt2 from '@/assets/images/landing/demo-receipt.png';
import FeaturePreview1 from '@/assets/images/landing/feature-1.png';
import FeaturePreview3 from '@/assets/images/landing/feature-3.png';
import FeaturePreview4 from '@/assets/images/landing/feature-4.png';
import HomePreview from '@/assets/images/landing/home-preview.png';

const LandingPage = () => {
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const id = entry.target.id;
            window.history.replaceState(null, '', `#${id}`);
          }
        });
      },
      {
        root: null,
        rootMargin: '0px',
        threshold: 0.5,
      },
    );

    document
      .querySelectorAll('section[id]')
      .forEach((section) => observer.observe(section));
    return () => observer.disconnect();
  }, []);

  return (
    <main className="bg-cool-neutral-99">
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
      className="flex flex-col gap-10 px-10 py-10 md:flex-row md:justify-between md:px-35 md:py-27.5"
    >
      <div className="title1-bold flex flex-col md:block">
        <div className="mb-4 flex flex-col">
          <h1 className="text-primary-normal">
            교환학생<span className="text-black">의</span>
          </h1>
          <h1>모든 카드 지출을</h1>
          <div className="flex items-center gap-2">
            <span className="h-0.75 w-33.75 bg-black" />
            <h1>하나로</h1>
          </div>
        </div>
        <h3 className="body1-normal-medium text-label-neutral mb-8 truncate">
          교환학생 지출의 모든 것, 통합 가계부 유니포켓
        </h3>
        <div className="flex gap-2.5">
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
      <img src={HomePreview} alt="Landing Preview" height={417} width={586} />
    </section>
  );
};

const FeatureSection = () => {
  return (
    <section id="features" className="flex flex-col p-10 md:px-35 md:py-45">
      <h2 className="title1-medium mb-6 text-center">
        교환학생 해외 지출을 어떻게 정리해야 할지 고민되나요?
      </h2>
      <h2 className="text-primary-normal mb-27.5 text-center text-3xl font-medium">
        유니포켓에서 쉽게 시작해봐요!
      </h2>
      <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
        {/* 1번 카드: 이미지 형태 */}
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
            src={FeaturePreview1}
            alt="Preview 1"
            className="animate-float absolute right-12.25 bottom-10 w-40 object-contain md:w-67.5"
          />
        </FeatureCard>

        {/* 2번 카드: 애니메이션 형태 */}
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
          {/* 가운데 정렬이 필요하다면 아래처럼 감싸서 배치 */}
          <div className="absolute bottom-20 left-0 w-full">
            <InfiniteCurrency />
          </div>
        </FeatureCard>

        {/* 3번 카드: 분석 리포트 */}
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
            src={FeaturePreview3}
            alt="Preview 1"
            className="absolute -bottom-35.25 left-50 w-163.5 object-contain md:h-104"
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
            src={FeaturePreview4}
            alt="Preview 1"
            className="animate-float absolute right-12.25 bottom-10 w-30 object-contain md:w-47.5"
          />
        </FeatureCard>
      </div>
    </section>
  );
};

const DemoSection = () => {
  return (
    <section id="demo" className="flex flex-col p-10 md:px-10 md:py-32.75">
      <h2 className="title1-medium mb-4 text-center">
        지출 내역을 올려보고 기능을 체험해보세요!
      </h2>
      <h2 className="mb-22.5 text-center text-2xl font-medium text-gray-400">
        영수증, 카드 지출 내역 이미지, CSV 파일을 업로드하고
        <br /> 어떻게 정리해주는지 알 수 있습니다.
      </h2>
      <div className="mb-7 flex justify-center gap-2.5">
        <Button variant="solid" size="lg">
          영수증 / 은행 앱 사진 업로드
        </Button>
        <Button variant="outlined" size="lg">
          거래 내역 파일 업로드
        </Button>
      </div>
      <div className="bg-background-normal flex flex-col gap-12 rounded-3xl p-3 pb-7">
        <div className="bg-background-alternative flex items-center justify-center py-20">
          <Icons.UploadFile className="text-label-alternative mr-4 h-12 w-12" />
          <div className="text-label-alternative">
            <h3 className="heading2-bold">
              클릭하거나 드래그해서 이미지를 업로드해주세요
            </h3>
            <h3 className="headline1-medium">
              jpg, jpeg, png, heic (최대 3개, 총 20MB 이하)
            </h3>
          </div>
        </div>
        <div className="px-4.25">
          <TabProvider variant="underline" defaultValue="sample1">
            <TabList>
              <TabTrigger value="sample1">Sample1.png</TabTrigger>
              <TabTrigger value="sample2">Sample2.png</TabTrigger>
            </TabList>
            <div className="h-10" />
            <TabContent value="sample1">
              <div className="flex gap-4.5">
                <div className="bg-background-alternative p-2.5">
                  <img src={DemoReceipt1} />
                  <DataTableProvider
                    columns={[]}
                    data={[]}
                    floatingBarVariant={'MANAGEMENT'}
                  >
                    <DataTable />
                  </DataTableProvider>
                </div>
              </div>
            </TabContent>
            <TabContent value="sample2">
              <div className="bg-background-alternative p-2.5">
                <img
                  src={DemoReceipt2}
                  alt="영수증 이미지"
                  className="h-8 w-8 cursor-pointer rounded-full object-cover"
                />
              </div>
            </TabContent>
          </TabProvider>
        </div>
      </div>
    </section>
  );
};
