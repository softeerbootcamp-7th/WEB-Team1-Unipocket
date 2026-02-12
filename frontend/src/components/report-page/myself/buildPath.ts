export interface ChartItem {
  date: string;
  cumulatedAmount: string;
}
export const buildLinePath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
  maxDay: number,
) => {
  const stepX = width / (maxDay - 1);

  return data
    .map((item, index) => {
      const x = stepX * index;
      const value = Number(item.cumulatedAmount);
      const y = height - (value / maxValue) * height;

      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');
};

export const buildAreaPath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
  maxDay: number,
) => {
  const stepX = width / (maxDay - 1);
  const linePath = data
    .map((item, index) => {
      const x = stepX * index;
      const value = Number(item.cumulatedAmount);
      const y = height - (value / maxValue) * height;

      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');

  const lastIndex = data.length - 1;
  const lastX = stepX * lastIndex;
  const closingPath = `L ${lastX} ${height} L 0 ${height} Z`;

  return linePath + ' ' + closingPath;
};
