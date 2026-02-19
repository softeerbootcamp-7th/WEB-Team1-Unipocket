import * as React from 'react';

import { cn } from '@/lib/utils';

function Table({ ...props }: React.ComponentProps<'table'>) {
  return (
    <div
      data-slot="table-container"
      className="scrollbar flex min-h-0 flex-1 flex-col overflow-auto"
    >
      <table data-slot="table" className="w-full" {...props} />
    </div>
  );
}

function TableHeader({ className, ...props }: React.ComponentProps<'thead'>) {
  return (
    <thead
      data-slot="table-header"
      className={cn(
        '[&_tr]:border-line-solid-neutral caption1-medium z-header sticky top-0',
        className,
      )}
      {...props}
    />
  );
}

function TableBody({ className, ...props }: React.ComponentProps<'tbody'>) {
  return (
    <tbody
      data-slot="table-body"
      className={cn(
        '[&_tr]:border-line-solid-neutral [&_tr:last-child]:border-0',
        className,
      )}
      {...props}
    />
  );
}

function TableFooter({ className, ...props }: React.ComponentProps<'tfoot'>) {
  return (
    <tfoot
      data-slot="table-footer"
      className={cn('[&>tr]:last:border-b-0', className)}
      {...props}
    />
  );
}

function TableRow({ className, ...props }: React.ComponentProps<'tr'>) {
  return (
    <tr
      data-slot="table-row"
      className={cn(
        'group/row',
        'bg-background-normal border-b transition-colors',
        'hover:bg-fill-alternative data-group-header:hover:bg-background-normal',
        'data-[state=selected]:bg-primary-normal/8 data-[state=selected]:hover:bg-primary-normal/12',
        'data-[state=error]:bg-status-negative/8 data-[state=error]:hover:bg-status-negative/12',
        className,
      )}
      {...props}
    />
  );
}

function TableHead({ className, ...props }: React.ComponentProps<'th'>) {
  return (
    <th
      data-slot="table-head"
      className={cn(
        'bg-background-normal px-3 py-3',
        'caption1-medium text-label-normal text-left align-middle whitespace-nowrap',
        '[&:has([role=checkbox])]:pr-0 *:[[role=checkbox]]:translate-y-0.5',
        className,
      )}
      {...props}
    />
  );
}

function TableCell({ className, ...props }: React.ComponentProps<'td'>) {
  return (
    <td
      data-slot="table-cell"
      className={cn(
        'text-label-normal label1-normal-medium whitespace-nowrap',
        'px-3 py-3.5 align-middle',
        '[&:has([role=checkbox])]:pr-0 *:[[role=checkbox]]:translate-y-0.5',
        className,
      )}
      {...props}
    />
  );
}

function TableCaption({
  className,
  ...props
}: React.ComponentProps<'caption'>) {
  return (
    <caption
      data-slot="table-caption"
      className={cn('text-muted-foreground text-sm', className)}
      {...props}
    />
  );
}

export {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
};
