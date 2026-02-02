import type React from "react";

import Category from "../Category";

interface ValueItemProps {
  label: string;
  value: React.ReactNode;
}

// 임시 데이터
const VALUE_ITEM_OPTIONS: ValueItemProps[] = [
  {
    label: '일시',
    value: '2025.12.02. (수) 13:26',
  },
  {
    label: '카테고리',
    value: <Category type="생활" />,
  },
  {
    label: '결제 수단',
    value: '하나 비바 X',
  },
  {
    label: '여행',
    value: '뉴욕',
  },
];


const ValueItem = ({ label, value }: ValueItemProps) => {
  return (
    <div className="flex h-8 items-center">
        <p className="w-25 label1-normal-bold text-label-alternative">{label}</p>
        <p className="w-63 px-1 label1-normal-medium text-label-normal">{value}</p>
    </div>
  )
}

const ValueContainer = () => {
  return (
    <div className="flex flex-col gap-2">
      {VALUE_ITEM_OPTIONS.map(({ label, value }) => (
        <ValueItem
          key={label}
          label={label}
          value={value}
        />
      ))}
    </div>
  );
};

export default ValueContainer;
