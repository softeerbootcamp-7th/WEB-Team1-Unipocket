import { clsx } from 'clsx';

interface DividerProps {
  style: 'thin' | 'thick' | 'vertical';
  className?: string;
}

const DIVIDER_STYLE = {
  thin: 'h-px w-full bg-line-normal-normal',
  thick: 'h-3 w-full bg-line-normal-alternative',
  vertical: 'w-px bg-line-normal-normal',
} as const;

const Divider = ({ style, className }: DividerProps) => {
  return <div className={clsx('shrink-0', DIVIDER_STYLE[style], className)} />;
};

export default Divider;
