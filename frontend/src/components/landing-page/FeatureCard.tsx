interface FeatureCardProps {
  index: number;
  title: React.ReactNode;
  children: React.ReactNode;
}

const FeatureCard = ({ index, title, children }: FeatureCardProps) => {
  return (
    <div className="relative flex h-100 flex-col overflow-hidden rounded-2xl bg-white px-11 py-9 shadow-md md:h-124.5">
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
export default FeatureCard;
