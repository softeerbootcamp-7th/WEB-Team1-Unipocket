import * as React from 'react';
import { Tooltip as TooltipPrimitive } from 'radix-ui';

import { Icons } from '@/assets';
import { cn } from '@/lib/utils';

function TooltipProvider({
  delayDuration = 0,
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Provider>) {
  return (
    <TooltipPrimitive.Provider
      data-slot="tooltip-provider"
      delayDuration={delayDuration}
      {...props}
    />
  );
}

function TooltipRoot({
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Root>) {
  return <TooltipPrimitive.Root data-slot="tooltip" {...props} />;
}

function TooltipTrigger({
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Trigger>) {
  return <TooltipPrimitive.Trigger data-slot="tooltip-trigger" {...props} />;
}

function TooltipContent({
  className,
  sideOffset = 0,
  children,
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Content>) {
  return (
    <TooltipPrimitive.Portal>
      <TooltipPrimitive.Content
        data-slot="tooltip-content"
        sideOffset={sideOffset}
        className={cn(
          'animate-in fade-in-0 zoom-in-95 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 rounded-modal-8 label1-normal-medium z-50 w-fit min-w-16 origin-(--radix-tooltip-content-transform-origin) bg-[#333435] p-2.5 text-balance text-[#F7F7F8]',
          className,
        )}
        {...props}
      >
        {children}
        {/* <TooltipPrimitive.Arrow className="z-50 size-3 translate-y-[calc(-50%-3px)] rotate-45 rounded-[2px] bg-[#333435] fill-[#333435]" /> */}
        <TooltipPrimitive.Arrow asChild>
          <Icons.Arrow
            className="translate-y-[-1.8px] rotate-180 text-[#333435]"
            style={{ width: 24, height: 8 }}
          />
        </TooltipPrimitive.Arrow>
      </TooltipPrimitive.Content>
    </TooltipPrimitive.Portal>
  );
}

export { TooltipContent, TooltipProvider, TooltipRoot, TooltipTrigger };
