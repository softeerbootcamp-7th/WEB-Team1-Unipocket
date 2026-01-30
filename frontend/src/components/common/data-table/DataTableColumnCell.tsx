import type { ComponentPropsWithoutRef } from 'react';

import { cn } from '@/lib/utils';

export function DataTableColumnCell({
  children,
  className,
}: ComponentPropsWithoutRef<'div'>) {
  return (
    <div className={cn('label1-normal-medium flex items-center', className)}>
      {children}
    </div>
  );
}
