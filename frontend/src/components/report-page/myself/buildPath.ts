import { type ChartItem } from '@/components/report-page/reportType';

const buildLineSegments = (
  data: ChartItem[],
  stepX: number,
  height: number,
  maxValue: number,
) =>
  data
    .map((item, index) => {
      const x = stepX * index;
      const value = Number(item.cumulatedAmount);
      const y = height - (value / maxValue) * height;

      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');

export const buildLinePath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
  maxDay: number,
) => {
  const stepX = width / (maxDay - 1);

  return buildLineSegments(data, stepX, height, maxValue);
};

export const buildAreaPath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
  maxDay: number,
) => {
  const stepX = width / (maxDay - 1);
  const linePath = buildLineSegments(data, stepX, height, maxValue);

  const lastIndex = data.length - 1;
  const lastX = stepX * lastIndex;
  const closingPath = `L ${lastX} ${height} L 0 ${height} Z`;

  return linePath + ' ' + closingPath;
};
