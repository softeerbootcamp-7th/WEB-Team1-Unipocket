import { useEffect } from 'react';
import { Link } from '@tanstack/react-router';

import Button from '@/components/common/Button';

import LandingPreview1 from '@/assets/images/landing-feature-1.png';
import LandingPreview3 from '@/assets/images/landing-feature-3.png';
import LandingPreview4 from '@/assets/images/landing-feature-4.png';
import PreviewImage from '@/assets/images/landing-preview.png';

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
      { rootMargin: '-45% 0px -45% 0px', threshold: 0 },
    );

    document
      .querySelectorAll('section[id]')
      .forEach((s) => observer.observe(s));
    return () => observer.disconnect();
  }, []);

  return (
    <main className="bg-cool-neutral-99">
      <section id="home" className="flex justify-between px-35 py-27.5">
        <div className="title1-bold flex flex-col truncate">
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
          <h3 className="body1-normal-medium text-label-neutral mb-8">
            교환학생 지출의 모든 것, 통합 가계부 유니포켓
          </h3>
          <div className="flex gap-2.5">
            <Link to="/login">
              <Button variant="solid" size="lg">
                로그인
              </Button>
            </Link>
            <Link to="/" hash="demo">
              <Button variant="solid" size="lg">
                데모 체험하기
              </Button>
            </Link>
          </div>
        </div>
        <img
          src={PreviewImage}
          alt="Landing Preview"
          height={417}
          width={586}
        />
      </section>
      <section id="features" className="flex flex-col px-35 py-45">
        <h2 className="title1-medium mb-6 text-center">
          교환학생 해외 지출을 어떻게 정리해야 할지 고민되나요?
        </h2>
        <h2 className="text-primary-normal mb-27.5 text-center text-3xl font-medium">
          유니포켓에서 쉽게 시작해봐요!
        </h2>
        <LandingFeatures />
      </section>
      <section
        id="demo"
        className="flex h-screen items-center justify-center bg-slate-50 text-2xl"
      >
        데모 체험
      </section>
    </main>
  );
};

export default LandingPage;

const LandingFeatures = () => {
  return (
    <div className="grid grid-cols-2 gap-5 bg-slate-50 p-10">
      {/* 1번 카드: 이미지 형태 */}
      <FeatureCard
        index={1}
        title={
          <span className="title3-semibold">
            <span className="text-primary-normal">이미지/CSV</span>로 업로드하여
            <br />
            지출 내역 한번에 업로드 가능!
          </span>
        }
      >
        <img
          src={LandingPreview1}
          alt="Preview 1"
          className="animate-float absolute right-12.25 bottom-10 w-67.5 object-contain"
        />
      </FeatureCard>

      {/* 2번 카드: 애니메이션 형태 */}
      <FeatureCard
        index={2}
        title={
          <span className="title3-semibold">
            소비 시점의 <span className="text-primary-normal">환율</span>로 자동
            계산!
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
            <span className="text-primary-normal">소비 분석 리포트</span>를 통해
            <br />
            한눈에 소비 행태 확인 가능!
          </span>
        }
      >
        <img
          src={LandingPreview3}
          alt="Preview 1"
          className="absolute -bottom-35.25 left-50 h-104 w-163.5 object-contain"
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
          src={LandingPreview4}
          alt="Preview 1"
          className="animate-float absolute right-12.25 bottom-10 w-47.5 object-contain"
        />
      </FeatureCard>
    </div>
  );
};

const InfiniteCurrency = () => {
  const currencies = [
    '₩ 1,474',
    '$ USD',
    'kr SEK',
    'kr NOK',
    'Fr CHF',
    '€ EUR',
    '£ GBP',
    '¥ JPY',
  ];

  return (
    <div className="mask-linear flex overflow-hidden whitespace-nowrap">
      {/* 두 번 반복하여 빈 공간 없이 이어지게 함 */}
      <div className="animate-marquee flex gap-4 pr-4">
        {currencies.map((c, i) => (
          <span
            key={i}
            className="rounded-full bg-slate-100 px-5 py-2 font-medium text-slate-600"
          >
            {c}
          </span>
        ))}
      </div>
      <div className="animate-marquee flex gap-4 pr-4" aria-hidden="true">
        {currencies.map((c, i) => (
          <span
            key={`dup-${i}`}
            className="rounded-full bg-slate-100 px-5 py-2 font-medium text-slate-600"
          >
            {c}
          </span>
        ))}
      </div>
    </div>
  );
};

interface FeatureCardProps {
  index: number;
  title: React.ReactNode;
  children: React.ReactNode;
}

const FeatureCard = ({ index, title, children }: FeatureCardProps) => {
  return (
    <div className="relative flex h-124.5 min-w-142.5 flex-col overflow-hidden rounded-2xl bg-white px-11 py-9 shadow-md">
      {/* 01, 02 인덱스 */}
      <span className="title3-medium text-label-alternative mb-3.5">
        {String(index).padStart(2, '0')}
      </span>

      {/* 제목 */}
      {title}

      {/* 내부 컨텐츠 (이미지, 애니메이션 등) */}
      {children}
    </div>
  );
};
