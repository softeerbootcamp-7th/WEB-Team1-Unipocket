import { useCallback, useMemo } from 'react';

import GapDropIndicator from '@/components/chart/widget/components/GapDropIndicator';
import type { DragData } from '@/components/chart/widget/hook/useWidgetDragAndDrop';
import { renderWidget } from '@/components/chart/widget/renderWidget';
import type { WidgetItem } from '@/components/chart/widget/type';
import {
  useWidgetContext,
  WidgetItemContext,
} from '@/components/chart/widget/WidgetContext';

interface WidgetListItemProps {
  widget: WidgetItem;
  isFirst: boolean;
}

const WidgetListItem = ({ widget, isFirst }: WidgetListItemProps) => {
  const {
    isWidgetEditMode,
    handleRemoveWidget,
    handleInsertWidget,
    handleMoveWidget,
  } = useWidgetContext();

  const isEditable = isWidgetEditMode && widget.widgetType !== 'BLANK';

  const onDropToGap = useCallback(
    (data: DragData, dropOrder: number) => {
      if (data.source === 'picker') {
        handleInsertWidget(data.widgetType, dropOrder);
      } else if (data.source === 'list' && data.order !== undefined) {
        handleMoveWidget(data.order, dropOrder);
      }
    },
    [handleInsertWidget, handleMoveWidget],
  );

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
      {isWidgetEditMode && isFirst && (
        <GapDropIndicator
          dropOrder={widget.order}
          onDropToGap={onDropToGap}
          position="left"
        />
      )}
      <WidgetItemContext.Provider value={value}>
        {renderWidget(widget)}
      </WidgetItemContext.Provider>
      {isWidgetEditMode && (
        <GapDropIndicator
          dropOrder={widget.order + 1}
          onDropToGap={onDropToGap}
          position="right"
        />
      )}
    </div>
  );
};

export default WidgetListItem;
