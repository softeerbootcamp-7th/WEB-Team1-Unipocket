import Tooltip from '@/components/common/Tooltip';

import { Icons } from '@/assets';

interface PageHeaderProps {
  title: string;
  subtitle: string;
  tooltipContent?: string;
}

const PageHeader = ({ title, subtitle, tooltipContent }: PageHeaderProps) => {
  return (
    <div className="flex flex-col justify-start gap-2.5">
      <div className="flex items-center justify-start gap-2.5">
        <h1 className="title2-semibold text-label-normal">{title}</h1>
        {tooltipContent && (
          <Tooltip content={tooltipContent} side="bottom">
            <Icons.Information className="size-6 cursor-pointer" />
          </Tooltip>
        )}
      </div>
      <h2 className="headline1-medium text-label-alternative">{subtitle}</h2>
    </div>
  );
};

export default PageHeader;
