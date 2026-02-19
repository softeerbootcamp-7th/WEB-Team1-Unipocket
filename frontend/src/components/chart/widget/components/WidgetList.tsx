import WidgetListItem from '@/components/chart/widget/components/WidgetListItem';
import { useWidgetContext } from '@/components/chart/widget/WidgetContext';

const WidgetList = () => {
  const { isWidgetEditMode, displayWidgets, listDropZone } = useWidgetContext();

  return (
    <div
      className="rounded-modal-20 flex w-full items-center justify-center gap-5 transition-colors"
      {...(isWidgetEditMode ? listDropZone.dropZoneProps : undefined)}
    >
      {displayWidgets.map((widget, i) => (
        <WidgetListItem
          key={widget.widgetType === 'BLANK' ? `BLANK-${i}` : widget.widgetType}
          widget={widget}
          isFirst={i === 0}
        />
      ))}
    </div>
  );
};

export default WidgetList;
