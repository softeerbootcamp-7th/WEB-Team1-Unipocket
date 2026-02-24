import { type IconName, Icons } from '@/assets';
import { cn } from '@/lib/utils';

interface IconProps {
  width?: number;
  height?: number;
  color?: string;
  iconName: IconName;
  ariaLabel?: string;
  className?: string;
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
  className,
  onClick,
}: IconProps) => {
  const IconComponent = Icons[iconName];

  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={ariaLabel ?? `${iconName} 버튼`}
      className={cn(
        color,
        'hover:bg-fill-normal flex cursor-pointer items-center justify-center rounded-lg transition-opacity',
        `w-[${width}px] h-[${height}px]`,
        className,
      )}
    >
      <IconComponent width={width} height={height} />
    </button>
  );
};

export default Icon;
