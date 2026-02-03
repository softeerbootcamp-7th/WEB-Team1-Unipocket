interface FeatureCardProps {
  index: number;
  title: React.ReactNode;
  children: React.ReactNode;
}

const FeatureCard = ({ index, title, children }: FeatureCardProps) => {
  return (
    <div className="shadow-card hover:animate-grow relative flex h-100 flex-col overflow-hidden rounded-2xl bg-white md:h-124.5">
      {/* 01, 02 인덱스 */}
      <span className="title3-medium text-label-alternative absolute top-9 left-11">
        {String(index).padStart(2, '0')}
      </span>

      {/* 제목 */}
      <div className="absolute top-20 left-11">{title}</div>

      {/* 내부 컨텐츠 (이미지, 애니메이션 등) */}
      {children}
    </div>
  );
};
export default FeatureCard;
