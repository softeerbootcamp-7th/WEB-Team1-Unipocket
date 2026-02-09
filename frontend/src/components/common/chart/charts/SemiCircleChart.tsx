interface SemiCircleChartProps {
  value: number;
  max?: number;
  color?: string;
  size?: number;
  children?: React.ReactNode;
}

const CHART_CONFIG = {
  STROKE_WIDTH: 12,
  TRACK_GAP: 4,
} as const;

const SemiCircleChart = ({
  value,
  max = 100,
  color = 'var(--color-primary-normal)',
  size = 200,
  children,
}: SemiCircleChartProps) => {
  const percentage = Math.min(Math.max(value / max, 0), 1);

  const strokeWidth = CHART_CONFIG.STROKE_WIDTH;

  // 반지름: (전체 너비 - 선 두께) / 2
  // 이렇게 해야 선이 SVG 영역 밖으로 튀어나가지 않고 딱 맞습니다.
  const radius = (size - strokeWidth) / 2;

  // 높이: 반지름 + 선 두께의 절반 (반원의 꼭대기까지)
  const semiHeight = radius + strokeWidth / 2;

  // 중심점 Y좌표: 바닥(높이)과 동일
  const centerY = semiHeight;

  // 시작점 X: 선 두께의 절반 (왼쪽 끝)
  const startX = strokeWidth / 2;

  // 끝점 X: 전체 너비 - 선 두께의 절반 (오른쪽 끝)
  const endX = size - strokeWidth / 2;

  // 180도 반원 경로 (왼쪽 -> 오른쪽)
  const dPath = `M ${startX} ${centerY} A ${radius} ${radius} 0 0 1 ${endX} ${centerY}`;

  const totalLength = Math.PI * radius;
  const filledLength = totalLength * percentage;

  // 트랙(배경) 점선 패턴 계산 (gap 적용)
  const trackStartOffset = filledLength + CHART_CONFIG.TRACK_GAP;
  const trackLength = Math.max(totalLength - trackStartOffset, 0);
  const trackDashArray = `0 ${trackStartOffset} ${trackLength} 0`;

  return (
    <div
      className="relative flex flex-col items-center justify-end"
      style={{ width: size, height: semiHeight }}
    >
      <svg
        width={size}
        height={semiHeight}
        viewBox={`0 0 ${size} ${semiHeight}`}
      >
        {/* 배경 */}
        <path
          d={dPath}
          fill="none"
          stroke="var(--color-fill-strong)"
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
          strokeDasharray={trackDashArray}
          className="transition-all duration-1000 ease-out"
        />

        {/* 데이터 게이지 */}
        <path
          d={dPath}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
          strokeDasharray={`${filledLength} ${totalLength}`}
          className="transition-all duration-1000 ease-out"
        />
      </svg>

      {/* 반원 라벨 */}
      <div className="absolute bottom-0">{children}</div>
    </div>
  );
};

export default SemiCircleChart;
