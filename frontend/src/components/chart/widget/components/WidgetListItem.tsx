import { useMemo } from 'react';

import GapDropIndicator from '@/components/chart/widget/components/GapDropIndicator';
import type { DragData } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import { renderWidget } from '@/components/chart/widget/renderWidget';
import type { WidgetItem } from '@/components/chart/widget/type';
import { WidgetItemContext } from '@/components/chart/widget/WidgetContext';

interface WidgetListItemProps {
  widget: WidgetItem;
  isWidgetEditMode: boolean;
  handleRemoveWidget: (order: number) => void;
  isFirst: boolean;
}

const WidgetListItem = ({
  widget,
  isWidgetEditMode,
  handleRemoveWidget,
  isFirst,
}: WidgetListItemProps) => {
  const isEditable = isWidgetEditMode && widget.widgetType !== 'BLANK';

  const value = useMemo(
    () => ({
      dragData: isEditable
        ? {
            widgetType: widget.widgetType as DragData['widgetType'],
            source: 'list' as const,
            order: widget.order,
          }
        : undefined,
      onRemove: isEditable ? () => handleRemoveWidget(widget.order) : undefined,
    }),
    [isEditable, widget.widgetType, widget.order, handleRemoveWidget],
  );

  return (
    <div className="relative">
      {isWidgetEditMode && isFirst && <GapDropIndicator position="left" />}
      <WidgetItemContext.Provider value={value}>
        {renderWidget(widget)}
      </WidgetItemContext.Provider>
      {isWidgetEditMode && <GapDropIndicator position="right" />}
    </div>
  );
};

export default WidgetListItem;
