import type { ComponentProps } from 'react';
import clsx from 'clsx';

interface ReportContainerProps extends ComponentProps<'div'> {
  title: string;
}

const ReportContainer = ({
  children,
  className,
  title,
  ...props
}: ReportContainerProps) => {
  return (
    <div
      {...props}
      className={clsx(
        'rounded-modal-16 bg-background-normal shadow-semantic-subtle flex flex-col gap-2.5 p-2 pt-4',
        className,
      )}
    >
      <span className="body2-normal-medium text-label-alternative px-2.5">
        {title}
      </span>
      {children}
    </div>
  );
};

export default ReportContainer;
