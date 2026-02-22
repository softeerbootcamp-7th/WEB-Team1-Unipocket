import Button from '@/components/common/Button';

const TravelHeader = () => {
  return (
    <div className="flex flex-col justify-center gap-11">
      <div className="flex flex-col justify-start gap-3">
        <h1 className="title2-semibold text-label-normal">여행 포켓</h1>
        <h2 className="headline1-medium text-label-alternative">
          여행별로 지출을 정리해두는 공간이에요
        </h2>
      </div>
      <div className="flex gap-4">
        <Button variant="solid" size="md">
          여행 포켓 생성하기
        </Button>
        <Button variant="outlined" size="md">
          편집
        </Button>
      </div>
    </div>
  );
};

export default TravelHeader;
