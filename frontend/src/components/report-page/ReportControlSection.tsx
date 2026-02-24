import Switch from '@/components/common/Switch';

import { getNavButtonClass } from '@/pages/report-page/report.utils';

import type { CurrencyType } from '@/types/currency';

import { Icons } from '@/assets';

interface ReportControlSectionProps {
  year: number;
  month: number;
  canGoPrev: boolean;
  canGoNext: boolean;
  onMonthChange: (offset: number) => void;
  currencyType: CurrencyType;
  onCurrencyChange: (checked: boolean) => void;
}

const ReportControlSection = ({
  year,
  month,
  canGoPrev,
  canGoNext,
  onMonthChange,
  currencyType,
  onCurrencyChange,
}: ReportControlSectionProps) => {
  return (
    //  {/* control section */}
    <div className="flex items-center justify-between">
      <div className="flex items-center gap-[21.5px]">
        <span className="title3-medium text-label-normal min-w-34">
          {year}년 {month}월
        </span>
        <div className="flex gap-3.5">
          <Icons.ChevronBack
            className={getNavButtonClass(canGoPrev)}
            onClick={() => canGoPrev && onMonthChange(-1)}
          />
          <Icons.ChevronForward
            className={getNavButtonClass(canGoNext)}
            onClick={() => canGoNext && onMonthChange(1)}
          />
        </div>
      </div>
      <div className="flex gap-3">
        <span className="body1-normal-medium text-label-alternative">
          현지통화로 보기
        </span>
        <Switch
          checked={currencyType === 'LOCAL'}
          onChange={(checked) => onCurrencyChange(checked)}
        />
      </div>
    </div>
  );
};

export default ReportControlSection;
