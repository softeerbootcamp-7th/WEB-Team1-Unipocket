import clsx from 'clsx';

import { type IconName, Icons } from '@/assets';

interface IconProps {
  width?: number;
  height?: number;
  color?: string;
  iconName: IconName;
  ariaLabel?: string;
  onClick?: () => void;
}

/**
 * Icon 컴포넌트
 *
 * hover 효과와 클릭 동작이 필요할 때 사용합니다.
 * 단순 아이콘은 Icons 컴포넌트를 직접 사용해도 됩니다.
 *
 * @example
 * <Icon iconName="Close" width={12} onClick={() => handleClose()} />
 */
const Icon = ({
  width = 24,
  height = 24,
  color = 'text-label-alternative',
  iconName,
  ariaLabel,
  onClick,
}: IconProps) => {
  const IconComponent = Icons[iconName];

  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={ariaLabel ?? `${iconName} 버튼`}
      className={clsx(
        color,
        'hover:bg-fill-normal flex items-center justify-center rounded-lg transition-opacity',
        `w-[${width}px] h-[${height}px]`,
      )}
    >
      <IconComponent width={width} height={height} />
    </button>
  );
};

export default Icon;
