import React from 'react';
import { clsx } from 'clsx';

export const ActionButton = React.forwardRef<
  HTMLButtonElement,
  React.ComponentProps<'button'>
>(({ children, onClick, className, ...props }, ref) => {
  return (
    <button
      ref={ref}
      onClick={onClick}
      className={clsx('label1-normal-medium outline-none', className)}
      {...props}
    >
      {children}
    </button>
  );
});
ActionButton.displayName = 'ActionButton';
