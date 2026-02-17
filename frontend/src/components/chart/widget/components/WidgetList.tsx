import { useCallback } from 'react';

import WidgetListItem from '@/components/chart/widget/components/WidgetListItem';
import type { DragData } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import type { WidgetItem, WidgetType } from '@/components/chart/widget/type';
import { useWidgetContext } from '@/components/chart/widget/WidgetContext';

interface WidgetListProps {
  displayWidgets: WidgetItem[];
  handleRemoveWidget: (order: number) => void;
  handleInsertWidget: (widgetType: WidgetType, targetOrder: number) => void;
  handleMoveWidget: (fromOrder: number, toOrder: number) => void;
  dropZoneProps?: React.HTMLAttributes<HTMLDivElement>;
}

const WidgetList = ({
  displayWidgets,
  handleRemoveWidget,
  handleInsertWidget,
  handleMoveWidget,
  dropZoneProps,
}: WidgetListProps) => {
  const { isWidgetEditMode } = useWidgetContext();

  const onDropToGap = useCallback(
    (data: DragData, dropOrder: number) => {
      if (data.source === 'picker') {
        // picker에서 드래그해서 특정 위치에 삽입
        handleInsertWidget(data.widgetType, dropOrder);
      } else if (data.source === 'list' && data.order !== undefined) {
        // list 내부에서 순서 이동
        handleMoveWidget(data.order, dropOrder);
      }
    },
    [handleInsertWidget, handleMoveWidget],
  );

  return (
    <div
      className="rounded-modal-20 flex w-full items-center justify-center gap-5 transition-colors"
      {...dropZoneProps}
    >
      {displayWidgets.map((widget, i) => (
        <WidgetListItem
          key={widget.widgetType === 'BLANK' ? `BLANK-${i}` : widget.widgetType}
          widget={widget}
          isWidgetEditMode={isWidgetEditMode}
          handleRemoveWidget={handleRemoveWidget}
          onDropToGap={onDropToGap}
          isFirst={i === 0}
        />
      ))}
    </div>
  );
};

export default WidgetList;
