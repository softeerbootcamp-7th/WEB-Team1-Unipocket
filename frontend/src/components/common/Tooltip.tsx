import {
  TooltipContent,
  TooltipRoot,
  TooltipTrigger,
} from '@/components/ui/tooltip';

interface Props {
  children: React.ReactNode;
  content: string;
  side?: 'top' | 'bottom' | 'left' | 'right';
  align?: 'start' | 'center' | 'end';
}

const Tooltip = ({
  children,
  content,
  side = 'bottom',
  align = 'start',
}: Props) => {
  return (
    <TooltipRoot>
      <TooltipTrigger asChild>{children}</TooltipTrigger>
      <TooltipContent
        side={side}
        align={align}
        sideOffset={2}
        className="flex items-start gap-2"
      >
        <p>{content}</p>
      </TooltipContent>
    </TooltipRoot>
  );
};

export default Tooltip;
