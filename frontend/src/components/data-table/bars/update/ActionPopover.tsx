import React, { useState } from 'react';

import { Popover, PopoverTrigger } from '@/components/ui/popover';

interface ActionPopoverProps {
  children: React.ReactNode;
  renderContent: (close: () => void) => React.ReactNode;
}

export const ActionPopover = ({
  children,
  renderContent,
}: ActionPopoverProps) => {
  const [open, setOpen] = useState(false);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>{children}</PopoverTrigger>
      {renderContent(() => setOpen(false))}
    </Popover>
  );
};
