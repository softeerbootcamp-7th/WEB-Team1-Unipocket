import { useState } from 'react';

import Button from '@/components/common/Button';
import {
  Popover,
  PopoverArrow,
  PopoverContentRaw,
  PopoverTrigger,
} from '@/components/ui/popover';
import {
  ARROW_CLASS,
  ARROW_STYLE,
  BUBBLE_CLASS,
} from '@/components/ui/tooltip';

import { Icons } from '@/assets';
import { cn } from '@/lib/utils';

interface ActionProps {
  label: string;
  onClick: () => void;
}

interface Props {
  children: React.ReactNode;
  content: string;
  secondaryAction: ActionProps;
  primaryAction: ActionProps;
  side?: 'top' | 'bottom' | 'left' | 'right';
  align?: 'start' | 'center' | 'end';
}

const Coachmark = ({
  children,
  content,
  secondaryAction,
  primaryAction,
  side = 'bottom',
  align = 'start',
}: Props) => {
  const [open, setOpen] = useState(true);

  const handleAction = (callback?: () => void) => () => {
    callback?.();
    setOpen(false);
  };

  return (
    <Popover open={open} modal={false}>
      <PopoverTrigger asChild>{children}</PopoverTrigger>
      <PopoverContentRaw
        side={side}
        align={align}
        sideOffset={2}
        onPointerDownOutside={(e) => e.preventDefault()}
        onInteractOutside={(e) => e.preventDefault()}
        className={cn(
          BUBBLE_CLASS,
          'origin-(--radix-popover-content-transform-origin) outline-hidden',
          'flex items-start gap-2',
        )}
      >
        <div className="flex flex-col gap-2">
          <p>{content}</p>

          <div className="flex items-center gap-1.5">
            <Button
              variant="outlined-inverse"
              onClick={handleAction(secondaryAction.onClick)}
            >
              {secondaryAction.label}
            </Button>
            <Button
              variant="solid"
              onClick={handleAction(primaryAction.onClick)}
            >
              {primaryAction.label}
            </Button>
          </div>
        </div>
        <PopoverArrow asChild>
          <Icons.Arrow className={ARROW_CLASS} style={ARROW_STYLE} />
        </PopoverArrow>
      </PopoverContentRaw>
    </Popover>
  );
};

export default Coachmark;
