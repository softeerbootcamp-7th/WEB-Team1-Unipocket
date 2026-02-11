export interface ChartItem {
  date: string;
  cumulatedAmount: string;
}

export const buildLinePath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
) => {
  const stepX = width / (data.length - 1);

  return data
    .map((item, index) => {
      const x = stepX * index;
      const value = Number(item.cumulatedAmount);
      const y = height - (value / maxValue) * height;

      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');
};
