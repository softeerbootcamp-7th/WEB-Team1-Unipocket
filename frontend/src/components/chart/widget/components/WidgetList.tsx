import { useMemo } from 'react';

import type { DragData } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import { renderWidget } from '@/components/chart/widget/renderWidget';
import type { WidgetItem } from '@/components/chart/widget/type';
import { WidgetItemContext } from '@/components/chart/widget/WidgetContext';

interface WidgetListProps {
  displayWidgets: WidgetItem[];
  isWidgetEditMode: boolean;
  handleRemoveWidget: (order: number) => void;
  dropZoneProps?: React.HTMLAttributes<HTMLDivElement>;
}

const WidgetList = ({
  displayWidgets,
  isWidgetEditMode,
  handleRemoveWidget,
  dropZoneProps,
}: WidgetListProps) => {
  return (
    <div
      className="rounded-modal-20 flex w-full items-center justify-between transition-colors"
      {...dropZoneProps}
    >
      {displayWidgets.map((widget) => (
        <WidgetListItem
          key={widget.order}
          widget={widget}
          isWidgetEditMode={isWidgetEditMode}
          handleRemoveWidget={handleRemoveWidget}
        />
      ))}
    </div>
  );
};

export default WidgetList;

interface WidgetListItemProps {
  widget: WidgetItem;
  isWidgetEditMode: boolean;
  handleRemoveWidget: (order: number) => void;
}

const WidgetListItem = ({
  widget,
  isWidgetEditMode,
  handleRemoveWidget,
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
    <WidgetItemContext.Provider value={value}>
      {renderWidget(widget)}
    </WidgetItemContext.Provider>
  );
};
