import { renderWidget } from '@/components/chart/widget/renderWidget';
import type { WidgetItem } from '@/components/chart/widget/type';

interface WidgetListProps {
  displayWidgets: WidgetItem[];
  isWidgetEditMode: boolean;
  handleRemoveWidget: (order: number) => void;
}

const WidgetList = ({
  displayWidgets,
  isWidgetEditMode,
  handleRemoveWidget,
}: WidgetListProps) => {
  return (
    <div className="flex items-center justify-around">
      {displayWidgets
        .sort((a, b) => a.order - b.order)
        .map((widget) => (
          <div
            key={widget.order}
            onClick={
              isWidgetEditMode && widget.widgetType !== 'BLANK'
                ? () => handleRemoveWidget(widget.order)
                : undefined
            }
            className={
              isWidgetEditMode && widget.widgetType !== 'BLANK'
                ? 'cursor-pointer'
                : ''
            }
          >
            {renderWidget(widget)}
          </div>
        ))}
    </div>
  );
};

export default WidgetList;
