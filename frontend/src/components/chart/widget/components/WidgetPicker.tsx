import { useMemo } from 'react';

import { renderWidget } from '@/components/chart/widget/renderWidget';
import type { WidgetType } from '@/components/chart/widget/type';
import { WidgetItemContext } from '@/components/chart/widget/WidgetContext';

interface WidgetPickerProps {
  maxWidgets: number;
  availableWidgets: WidgetType[];
  dropZoneProps?: React.HTMLAttributes<HTMLDivElement>;
}

const WidgetPicker = ({
  maxWidgets,
  availableWidgets,
  dropZoneProps,
}: WidgetPickerProps) => {
  return (
    <div
      className="rounded-modal-20 flex h-full w-full min-w-0 flex-col justify-start gap-5 px-10 py-9 transition-colors"
      {...dropZoneProps}
    >
      <WidgetPickerHeader maxWidgets={maxWidgets} />
      <div className="w-px min-w-full overflow-x-auto">
        <div className="flex w-max gap-8 p-1">
          {availableWidgets.map((widgetType) => (
            <WidgetPickerItem key={widgetType} widgetType={widgetType} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default WidgetPicker;

const WidgetPickerHeader = ({ maxWidgets }: { maxWidgets: number }) => {
  return (
    <div className="flex flex-col gap-2.75">
      <h3 className="title3-semibold text-label-normal">추가 가능한 위젯들</h3>
      <span className="label1-normal-medium text-label-neutral">
        추가하고 싶은 위젯을 상단에 드래그 해주세요.
        <br />
        {`위젯은 최대 ${maxWidgets}개까지 설정할 수 있습니다.`}
      </span>
    </div>
  );
};

const WidgetPickerItem = ({ widgetType }: { widgetType: WidgetType }) => {
  const value = useMemo(
    () => ({ dragData: { widgetType, source: 'picker' as const } }),
    [widgetType],
  );

  return (
    <WidgetItemContext.Provider value={value}>
      {renderWidget({ order: -1, widgetType }, { isPreview: true })}
    </WidgetItemContext.Provider>
  );
};
